package roomescape.domain.payment;

import roomescape.domain.RoomEscapeException;

import java.util.Optional;

import static roomescape.domain.DomainErrorCode.RESOURCE_NOT_FOUND;

public interface PaymentOrderRepository {
    PaymentOrder save(PaymentOrder order);

    Optional<PaymentOrder> findByOrderId(String orderId);

    Optional<PaymentOrder> findByReservationId(Long reservationId);

    void updatePaymentKey(Long id, String paymentKey);

    void deleteById(Long id);

    default PaymentOrder getByOrderId(String orderId) {
        return findByOrderId(orderId)
                .orElseThrow(() -> new RoomEscapeException(RESOURCE_NOT_FOUND, "해당 주문을 찾을 수 없습니다. : " + orderId));
    }
}
