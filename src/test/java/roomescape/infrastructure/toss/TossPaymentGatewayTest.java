package roomescape.infrastructure.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.domain.payment.PaymentAlreadyProcessedException;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentFailedException;
import roomescape.domain.payment.PaymentKeyConfigurationException;
import roomescape.domain.payment.PaymentRejectedException;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.payment.PaymentRetryableException;
import roomescape.domain.payment.PaymentStatus;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TossPaymentGatewayTest {
    private static final String BASE_URL = "https://api.test.local";
    private static final String SECRET_KEY = "test_sk_abcdef";
    private static final PaymentConfirmation CONFIRMATION =
            new PaymentConfirmation("payment-key-123", "order-abc123", 10000L);

    private MockRestServiceServer server;
    private TossPaymentGateway gateway;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = TossRestClientConfig.builder(BASE_URL, SECRET_KEY);
        server = MockRestServiceServer.bindTo(builder).build();
        gateway = new TossPaymentGateway(builder.build(), new ObjectMapper());
    }

    private String expectedBasicHeader() {
        return "Basic " + Base64.getEncoder()
                .encodeToString((SECRET_KEY + ":").getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void 승인_성공_응답을_PaymentResult로_변환한다() {
        server.expect(requestTo(BASE_URL + "/v1/payments/confirm"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("Authorization", expectedBasicHeader()))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.paymentKey").value("payment-key-123"))
                .andExpect(jsonPath("$.orderId").value("order-abc123"))
                .andExpect(jsonPath("$.amount").value(10000))
                .andRespond(withSuccess("""
                        {
                          "paymentKey": "payment-key-123",
                          "orderId": "order-abc123",
                          "totalAmount": 10000,
                          "status": "DONE",
                          "unknownField": {"nested": true},
                          "method": "카드"
                        }
                        """, MediaType.APPLICATION_JSON));

        PaymentResult result = gateway.confirm(CONFIRMATION);

        Assertions.assertThat(result.isDone()).isTrue();
        Assertions.assertThat(result.getPaymentKey()).isEqualTo("payment-key-123");
        Assertions.assertThat(result.getTotalAmount()).isEqualTo(10000L);
        server.verify();
    }

    @Test
    void 미정의_status는_UNKNOWN으로_매핑된다() {
        server.expect(requestTo(BASE_URL + "/v1/payments/confirm"))
                .andRespond(withSuccess("""
                        {"paymentKey": "pk", "orderId": "order-abc123", "totalAmount": 10000, "status": "SOMETHING_NEW"}
                        """, MediaType.APPLICATION_JSON));

        PaymentResult result = gateway.confirm(CONFIRMATION);

        Assertions.assertThat(result.getStatus()).isEqualTo(PaymentStatus.UNKNOWN);
        Assertions.assertThat(result.isDone()).isFalse();
    }

    @Test
    void 이미_처리된_결제_에러는_전용_예외로_변환된다() {
        respondError(HttpStatus.BAD_REQUEST, "ALREADY_PROCESSED_PAYMENT", "이미 처리된 결제 입니다.");

        Assertions.assertThatThrownBy(() -> gateway.confirm(CONFIRMATION))
                .isInstanceOf(PaymentAlreadyProcessedException.class)
                .hasMessage("이미 처리된 결제 입니다.");
    }

    @Test
    void 키_오류는_설정_예외로_변환된다() {
        respondError(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_KEY", "인증되지 않은 시크릿 키 혹은 클라이언트 키 입니다.");

        Assertions.assertThatThrownBy(() -> gateway.confirm(CONFIRMATION))
                .isInstanceOf(PaymentKeyConfigurationException.class);
    }

    @Test
    void 카드_거절은_거절_예외로_변환된다() {
        respondError(HttpStatus.FORBIDDEN, "REJECT_CARD_PAYMENT", "한도초과 혹은 잔액부족으로 결제에 실패했습니다.");

        Assertions.assertThatThrownBy(() -> gateway.confirm(CONFIRMATION))
                .isInstanceOf(PaymentRejectedException.class)
                .hasMessageContaining("한도초과");
    }

    @Test
    void 토스_내부_오류는_재시도_대상_예외로_변환된다() {
        respondError(HttpStatus.INTERNAL_SERVER_ERROR, "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", "결제가 완료되지 않았어요.");

        Assertions.assertThatThrownBy(() -> gateway.confirm(CONFIRMATION))
                .isInstanceOf(PaymentRetryableException.class);
    }

    @Test
    void 미정의_에러_코드는_기본_예외로_폴백한다() {
        respondError(HttpStatus.BAD_REQUEST, "BRAND_NEW_ERROR_CODE", "새로운 오류");

        Assertions.assertThatThrownBy(() -> gateway.confirm(CONFIRMATION))
                .isInstanceOf(PaymentFailedException.class);
    }

    @Test
    void 에러_본문을_해석할_수_없으면_기본_예외로_폴백한다() {
        server.expect(requestTo(BASE_URL + "/v1/payments/confirm"))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY)
                        .contentType(MediaType.TEXT_HTML)
                        .body("<html>Bad Gateway</html>"));

        Assertions.assertThatThrownBy(() -> gateway.confirm(CONFIRMATION))
                .isInstanceOf(PaymentFailedException.class);
    }

    private void respondError(HttpStatus status, String code, String message) {
        server.expect(requestTo(BASE_URL + "/v1/payments/confirm"))
                .andRespond(withStatus(status)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\": \"" + code + "\", \"message\": \"" + message + "\"}"));
    }
}
