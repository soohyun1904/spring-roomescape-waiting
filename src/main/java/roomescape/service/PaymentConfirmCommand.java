package roomescape.service;

public class PaymentConfirmCommand {
    private final String paymentKey;
    private final String orderId;
    private final Long amount;

    public PaymentConfirmCommand(String paymentKey, String orderId, Long amount) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
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
