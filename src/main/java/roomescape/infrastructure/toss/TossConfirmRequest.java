package roomescape.infrastructure.toss;

public class TossConfirmRequest {
    private final String paymentKey;
    private final String orderId;
    private final Long amount;

    public TossConfirmRequest(String paymentKey, String orderId, Long amount) {
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
