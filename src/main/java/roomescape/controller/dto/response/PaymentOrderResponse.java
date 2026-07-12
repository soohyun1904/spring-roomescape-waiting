package roomescape.controller.dto.response;

import roomescape.domain.payment.PaymentOrder;

public class PaymentOrderResponse {
    private final String orderId;
    private final Long amount;
    private final Long reservationId;

    public PaymentOrderResponse(String orderId, Long amount, Long reservationId) {
        this.orderId = orderId;
        this.amount = amount;
        this.reservationId = reservationId;
    }

    public static PaymentOrderResponse toDto(PaymentOrder order) {
        return new PaymentOrderResponse(order.getOrderId(), order.getAmount(), order.getReservationId());
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getAmount() {
        return amount;
    }

    public Long getReservationId() {
        return reservationId;
    }
}
