package roomescape.infrastructure.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentFailedException;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.payment.PaymentStatus;

import java.io.IOException;

@Component
public class TossPaymentGateway implements PaymentGateway {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
        this.restClient = tossRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        TossConfirmRequest request = new TossConfirmRequest(
                confirmation.getPaymentKey(),
                confirmation.getOrderId(),
                confirmation.getAmount());

        TossPaymentResponse response = restClient.post()
                .uri("/v1/payments/confirm")
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw TossErrorMapper.toException(readError(res));
                })
                .body(TossPaymentResponse.class);

        if (response == null) {
            throw new PaymentFailedException("결제 승인 응답이 비어 있습니다.");
        }
        return new PaymentResult(
                response.getPaymentKey(),
                response.getOrderId(),
                response.getTotalAmount(),
                PaymentStatus.from(response.getStatus()));
    }

    private TossErrorResponse readError(org.springframework.http.client.ClientHttpResponse response) {
        try {
            return objectMapper.readValue(response.getBody(), TossErrorResponse.class);
        } catch (IOException e) {
            return new TossErrorResponse(null, "결제 승인 응답을 해석할 수 없습니다.");
        }
    }
}
