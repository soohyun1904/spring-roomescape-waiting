package roomescape.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import roomescape.domain.RoomEscapeException;
import roomescape.domain.payment.PaymentAlreadyProcessedException;
import roomescape.domain.payment.PaymentAmountMismatchException;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentFailedException;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentOrder;
import roomescape.domain.payment.PaymentOrderRepository;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.payment.PaymentStatus;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    private static final Slot DUMMY_SLOT = Slot.load(
            1L,
            LocalDate.of(2099, 1, 1),
            ReservationTime.load(1L, LocalTime.of(10, 0)),
            Theme.load(1L, "any", "any", "https://zeze.com/thumb.jpg", 10000L)
    );
    private static final PaymentOrder ORDER = PaymentOrder.load(1L, "order-abc123", 10L, 10000L, null);
    private static final String PAYMENT_KEY = "payment-key-123";

    @Mock
    private PaymentGateway paymentGateway;
    @Mock
    private PaymentOrderRepository paymentOrderRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private TransactionTemplate transactionTemplate;
    @InjectMocks
    private PaymentService paymentService;

    private static Reservation reservation(Status status) {
        return new Reservation(10L, new roomescape.domain.reservation.ReservationName("zeze"), status, DUMMY_SLOT);
    }

    @SuppressWarnings("unchecked")
    private void givenTransactionRunsCallback() {
        given(transactionTemplate.execute(any())).willAnswer(invocation ->
                ((TransactionCallback<Reservation>) invocation.getArgument(0)).doInTransaction(null));
    }

    @Test
    void 결제_시작시_주문이_없으면_새로_생성한다() {
        given(reservationRepository.getById(10L)).willReturn(reservation(Status.PENDING_PAYMENT));
        given(paymentOrderRepository.findByReservationId(10L)).willReturn(Optional.empty());
        given(paymentOrderRepository.save(any())).willAnswer(invocation -> ((PaymentOrder) invocation.getArgument(0)).withId(1L));

        PaymentOrder order = paymentService.createOrder(10L);

        Assertions.assertThat(order.getAmount()).isEqualTo(10000L);
        Assertions.assertThat(order.getOrderId()).matches("[a-zA-Z0-9_-]{6,64}");
        Assertions.assertThat(order.getReservationId()).isEqualTo(10L);
    }

    @Test
    void 결제_시작시_기존_주문이_있으면_재사용한다() {
        given(reservationRepository.getById(10L)).willReturn(reservation(Status.PENDING_PAYMENT));
        given(paymentOrderRepository.findByReservationId(10L)).willReturn(Optional.of(ORDER));

        PaymentOrder order = paymentService.createOrder(10L);

        Assertions.assertThat(order).isEqualTo(ORDER);
        verify(paymentOrderRepository, never()).save(any());
    }

    @Test
    void 결제_대기_상태가_아닌_예약은_결제를_시작할_수_없다() {
        given(reservationRepository.getById(10L)).willReturn(reservation(Status.APPROVED));

        Assertions.assertThatThrownBy(() -> paymentService.createOrder(10L))
                .isInstanceOf(RoomEscapeException.class);
        verify(paymentOrderRepository, never()).save(any());
    }

    @Test
    void 금액이_일치하지_않으면_예외가_발생하고_게이트웨이는_호출되지_않는다() {
        given(paymentOrderRepository.getByOrderId("order-abc123")).willReturn(ORDER);

        Assertions.assertThatThrownBy(() -> paymentService.confirm(
                        new PaymentConfirmCommand(PAYMENT_KEY, "order-abc123", 99999L)))
                .isInstanceOf(PaymentAmountMismatchException.class);

        verify(paymentGateway, never()).confirm(any());
    }

    @Test
    void 존재하지_않는_주문이면_예외가_발생한다() {
        given(paymentOrderRepository.getByOrderId("order-none01")).willCallRealMethod();
        given(paymentOrderRepository.findByOrderId("order-none01")).willReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> paymentService.confirm(
                        new PaymentConfirmCommand(PAYMENT_KEY, "order-none01", 10000L)))
                .isInstanceOf(RoomEscapeException.class);

        verify(paymentGateway, never()).confirm(any());
    }

    @Test
    void 승인_성공시_paymentKey를_저장하고_예약을_확정한다() {
        given(paymentOrderRepository.getByOrderId("order-abc123")).willReturn(ORDER);
        given(paymentGateway.confirm(any()))
                .willReturn(new PaymentResult(PAYMENT_KEY, "order-abc123", 10000L, PaymentStatus.DONE));
        given(reservationRepository.getById(10L)).willReturn(reservation(Status.APPROVED));
        givenTransactionRunsCallback();

        Reservation confirmed = paymentService.confirm(new PaymentConfirmCommand(PAYMENT_KEY, "order-abc123", 10000L));

        Assertions.assertThat(confirmed.isApproved()).isTrue();
        verify(paymentOrderRepository).updatePaymentKey(1L, PAYMENT_KEY);
        verify(reservationRepository).updateStatusById(10L, Status.APPROVED);
    }

    @Test
    void 승인_요청_금액은_요청_값이_아니라_DB에_저장된_금액을_사용한다() {
        given(paymentOrderRepository.getByOrderId("order-abc123")).willReturn(ORDER);
        given(paymentGateway.confirm(any()))
                .willReturn(new PaymentResult(PAYMENT_KEY, "order-abc123", 10000L, PaymentStatus.DONE));
        given(reservationRepository.getById(10L)).willReturn(reservation(Status.APPROVED));
        givenTransactionRunsCallback();

        paymentService.confirm(new PaymentConfirmCommand(PAYMENT_KEY, "order-abc123", 10000L));

        ArgumentCaptor<PaymentConfirmation> captor = ArgumentCaptor.forClass(PaymentConfirmation.class);
        verify(paymentGateway).confirm(captor.capture());
        Assertions.assertThat(captor.getValue().getAmount()).isEqualTo(ORDER.getAmount());
    }

    @Test
    void 이미_처리된_결제는_예약이_확정_상태면_성공과_동일하게_응답한다() {
        given(paymentOrderRepository.getByOrderId("order-abc123")).willReturn(ORDER);
        given(paymentGateway.confirm(any())).willThrow(new PaymentAlreadyProcessedException("이미 처리된 결제입니다."));
        given(reservationRepository.getById(10L)).willReturn(reservation(Status.APPROVED));

        Reservation confirmed = paymentService.confirm(new PaymentConfirmCommand(PAYMENT_KEY, "order-abc123", 10000L));

        Assertions.assertThat(confirmed.isApproved()).isTrue();
        verify(paymentOrderRepository, never()).updatePaymentKey(any(), any());
    }

    @Test
    void 이미_처리된_결제라도_예약이_확정_상태가_아니면_예외가_전파된다() {
        given(paymentOrderRepository.getByOrderId("order-abc123")).willReturn(ORDER);
        given(paymentGateway.confirm(any())).willThrow(new PaymentAlreadyProcessedException("이미 처리된 결제입니다."));
        given(reservationRepository.getById(10L)).willReturn(reservation(Status.PENDING_PAYMENT));

        Assertions.assertThatThrownBy(() -> paymentService.confirm(
                        new PaymentConfirmCommand(PAYMENT_KEY, "order-abc123", 10000L)))
                .isInstanceOf(PaymentAlreadyProcessedException.class);
    }

    @Test
    void 승인_결과가_완료_상태가_아니면_예외가_발생한다() {
        given(paymentOrderRepository.getByOrderId("order-abc123")).willReturn(ORDER);
        given(paymentGateway.confirm(any()))
                .willReturn(new PaymentResult(PAYMENT_KEY, "order-abc123", 10000L, PaymentStatus.CANCELED));

        Assertions.assertThatThrownBy(() -> paymentService.confirm(
                        new PaymentConfirmCommand(PAYMENT_KEY, "order-abc123", 10000L)))
                .isInstanceOf(PaymentFailedException.class);

        verify(reservationRepository, never()).updateStatusById(any(), any());
    }

    @Test
    void orderId가_없으면_정리_요청은_무시된다() {
        paymentService.cancelPending(null);
        paymentService.cancelPending("  ");

        verifyNoInteractions(paymentOrderRepository, reservationRepository, paymentGateway);
    }

    @Test
    void 결제_대기_예약_정리시_예약이_삭제되고_첫_대기자가_승격된다() {
        Reservation pending = reservation(Status.PENDING_PAYMENT);
        Reservation waiting = new Reservation(11L, new roomescape.domain.reservation.ReservationName("mingu"),
                Status.WAITING, DUMMY_SLOT);
        given(paymentOrderRepository.findByOrderId("order-abc123")).willReturn(Optional.of(ORDER));
        given(reservationRepository.getById(10L)).willReturn(pending);
        given(reservationRepository.findBySlotId(1L))
                .willReturn(new roomescape.domain.reservation.Reservations(java.util.List.of(waiting)));

        paymentService.cancelPending("order-abc123");

        verify(reservationRepository).deleteById(10L);
        verify(reservationRepository).updateStatusById(11L, Status.APPROVED);
    }
}
