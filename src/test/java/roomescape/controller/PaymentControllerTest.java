package roomescape.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.controller.dto.request.PaymentConfirmRequest;
import roomescape.controller.dto.request.PaymentOrderCreateRequest;
import roomescape.controller.dto.request.PaymentFailRequest;
import roomescape.domain.payment.PaymentOrder;
import roomescape.domain.payment.PaymentAmountMismatchException;
import roomescape.domain.payment.PaymentKeyConfigurationException;
import roomescape.domain.payment.PaymentRejectedException;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Slot;
import roomescape.domain.theme.Theme;
import roomescape.service.PaymentService;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private PaymentService paymentService;

    private Reservation approvedReservation() {
        ReservationTime time = ReservationTime.load(1L, LocalTime.of(10, 0));
        Theme theme = Theme.load(1L, "공포", "무서워요", "https://zeze.com", 10000L);
        Slot slot = Slot.load(1L, LocalDate.of(2099, 1, 1), time, theme);
        return Reservation.load(1L, "zeze", "APPROVED", slot);
    }

    @Test
    void 결제_설정_조회시_클라이언트_키를_반환한다() throws Exception {
        mockMvc.perform(get("/payments/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientKey").value("test_ck_dummy"));
    }

    @Test
    void 결제_시작시_주문_정보를_반환한다() throws Exception {
        given(paymentService.createOrder(1L))
                .willReturn(PaymentOrder.load(1L, "order-abc123", 1L, 35000L, null));

        mockMvc.perform(post("/payments/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PaymentOrderCreateRequest(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order-abc123"))
                .andExpect(jsonPath("$.amount").value(35000))
                .andExpect(jsonPath("$.reservationId").value(1));
    }

    @Test
    void 결제_시작시_reservationId가_없으면_400을_반환한다() throws Exception {
        mockMvc.perform(post("/payments/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PaymentOrderCreateRequest(null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 결제_승인_성공시_확정된_예약을_반환한다() throws Exception {
        given(paymentService.confirm(any())).willReturn(approvedReservation());

        mockMvc.perform(post("/payments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PaymentConfirmRequest("payment-key", "order-abc123", 10000L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("승인"));
    }

    @Test
    void 결제_승인시_paymentKey가_없으면_400을_반환한다() throws Exception {
        mockMvc.perform(post("/payments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PaymentConfirmRequest(null, "order-abc123", 10000L))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 금액_불일치시_400을_반환한다() throws Exception {
        given(paymentService.confirm(any())).willThrow(new PaymentAmountMismatchException("금액 불일치"));

        mockMvc.perform(post("/payments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PaymentConfirmRequest("payment-key", "order-abc123", 99999L))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 카드_거절시_402를_반환한다() throws Exception {
        given(paymentService.confirm(any())).willThrow(new PaymentRejectedException("한도 초과"));

        mockMvc.perform(post("/payments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PaymentConfirmRequest("payment-key", "order-abc123", 10000L))))
                .andExpect(status().isPaymentRequired());
    }

    @Test
    void 키_설정_오류시_500과_안내_메시지를_반환한다() throws Exception {
        given(paymentService.confirm(any())).willThrow(new PaymentKeyConfigurationException("인증되지 않은 키"));

        mockMvc.perform(post("/payments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PaymentConfirmRequest("payment-key", "order-abc123", 10000L))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("결제 설정 오류가 발생했습니다. 관리자에게 문의해주세요."));
    }

    @Test
    void 결제_실패_정리시_orderId가_없어도_204를_반환한다() throws Exception {
        mockMvc.perform(post("/payments/fail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PaymentFailRequest("PAY_PROCESS_CANCELED", "사용자가 취소했습니다.", null))))
                .andExpect(status().isNoContent());

        verify(paymentService).cancelPending(null);
    }

    @Test
    void 결제_실패_정리시_orderId가_있으면_해당_주문을_정리한다() throws Exception {
        mockMvc.perform(post("/payments/fail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PaymentFailRequest("REJECT_CARD_PAYMENT", "카드 거절", "order-abc123"))))
                .andExpect(status().isNoContent());

        verify(paymentService).cancelPending("order-abc123");
    }
}
