package roomescape.service;

import roomescape.controller.dto.request.PaymentConfirmRequest;

public class PaymentConfirmCommand {
    private final String paymentKey;
    private final String orderId;
    private final Long amount;

    public PaymentConfirmCommand(String paymentKey, String orderId, Long amount) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
    }

    public static PaymentConfirmCommand from(PaymentConfirmRequest request) {
        return new PaymentConfirmCommand(request.getPaymentKey(), request.getOrderId(), request.getAmount());
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getAmount() {
        return amount;
    }
}
