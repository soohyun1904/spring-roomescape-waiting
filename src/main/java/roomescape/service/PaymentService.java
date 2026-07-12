package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import roomescape.domain.DomainErrorCode;
import roomescape.domain.RoomEscapeException;
import roomescape.domain.payment.PaymentAlreadyProcessedException;
import roomescape.domain.payment.PaymentAmountMismatchException;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentFailedException;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentOrder;
import roomescape.domain.payment.PaymentOrderRepository;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.Status;

@Service
public class PaymentService {
    private final PaymentGateway paymentGateway;
    private final PaymentOrderRepository paymentOrderRepository;
    private final ReservationRepository reservationRepository;
    private final TransactionTemplate transactionTemplate;

    public PaymentService(
            PaymentGateway paymentGateway,
            PaymentOrderRepository paymentOrderRepository,
            ReservationRepository reservationRepository,
            TransactionTemplate transactionTemplate
    ) {
        this.paymentGateway = paymentGateway;
        this.paymentOrderRepository = paymentOrderRepository;
        this.reservationRepository = reservationRepository;
        this.transactionTemplate = transactionTemplate;
    }

    // "결제를 시작하겠다"는 요청. 예약 생성과 분리되어 결제 페이지 진입 시점에 호출된다.
    // 결제 페이지 재진입·새로고침 시에도 같은 주문을 재사용하도록 예약당 하나의 주문만 생성한다(get-or-create, 멱등).
    @Transactional
    public PaymentOrder createOrder(Long reservationId) {
        Reservation reservation = reservationRepository.getById(reservationId);
        if (!reservation.isPendingPayment()) {
            throw new RoomEscapeException(DomainErrorCode.INVALID_INPUT,
                    "결제 대기 상태의 예약만 결제를 시작할 수 있습니다: " + reservationId);
        }
        // 금액은 클라이언트 입력이 아니라 서버가 테마 가격으로 확정한다 — 이후 금액 위변조 검증의 원본.
        return paymentOrderRepository.findByReservationId(reservationId)
                .orElseGet(() -> paymentOrderRepository.save(PaymentOrder.create(
                        reservationId, reservation.getSlot().getTheme().getPrice().getValue())));
    }

    public Reservation confirm(PaymentConfirmCommand command) {
        PaymentOrder order = paymentOrderRepository.getByOrderId(command.getOrderId());
        if (!order.isSameAmount(command.getAmount())) {
            throw new PaymentAmountMismatchException("결제 금액이 주문 금액과 일치하지 않습니다. 주문: "
                    + order.getOrderId() + ", 요청 금액: " + command.getAmount());
        }

        // 승인 요청 금액은 클라이언트가 보낸 값이 아니라 DB에 저장된 원본을 사용한다(방어선 이중화).
        PaymentConfirmation confirmation =
                new PaymentConfirmation(command.getPaymentKey(), order.getOrderId(), order.getAmount());

        PaymentResult result;
        try {
            result = paymentGateway.confirm(confirmation);
        } catch (PaymentAlreadyProcessedException e) {
            return recoverAlreadyProcessed(order, e);
        }

        if (!result.isDone()) {
            throw new PaymentFailedException("결제가 완료 상태가 아닙니다: " + result.getStatus());
        }

        return complete(order, result);
    }

    // 결제 승인(외부 호출)은 트랜잭션 밖에서 수행하고,
    // paymentKey 저장과 예약 확정은 하나의 트랜잭션으로 묶는다.
    private Reservation complete(PaymentOrder order, PaymentResult result) {
        return transactionTemplate.execute(status -> {
            paymentOrderRepository.updatePaymentKey(order.getId(), result.getPaymentKey());
            reservationRepository.updateStatusById(order.getReservationId(), Status.APPROVED);
            return reservationRepository.getById(order.getReservationId());
        });
    }

    // success 페이지 새로고침 등으로 승인 API가 중복 호출되는 정상 시나리오.
    // 이미 승인되어 예약이 확정된 주문이면 실패가 아니라 성공과 동일한 응답을 내려준다(멱등 처리).
    private Reservation recoverAlreadyProcessed(PaymentOrder order, PaymentAlreadyProcessedException e) {
        Reservation reservation = reservationRepository.getById(order.getReservationId());
        if (reservation.isApproved()) {
            return reservation;
        }
        throw e;
    }

    @Transactional
    public void cancelPending(String orderId) {
        // 사용자가 결제창에서 결제를 취소하면(PAY_PROCESS_CANCELED) orderId 없이 호출될 수 있다 — null 가드
        if (orderId == null || orderId.isBlank()) {
            return;
        }
        paymentOrderRepository.findByOrderId(orderId)
                .ifPresent(this::cancelPendingReservation);
    }

    private void cancelPendingReservation(PaymentOrder order) {
        Reservation reservation = reservationRepository.getById(order.getReservationId());
        if (!reservation.isPendingPayment()) {
            return;
        }
        // 주문(payment_order)은 예약 삭제 시 FK cascade로 함께 정리된다.
        reservationRepository.deleteById(reservation.getId());
        reservationRepository.findBySlotId(reservation.getSlotId())
                .firstWaiting()
                .ifPresent(waiting -> reservationRepository.updateStatusById(waiting.getId(), Status.APPROVED));
    }
}
