package roomescape.domain.payment;

public class PaymentResult {
    private final String paymentKey;
    private final String orderId;
    private final Long totalAmount;
    private final PaymentStatus status;

    public PaymentResult(String paymentKey, String orderId, Long totalAmount, PaymentStatus status) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public boolean isDone() {
        return status.isDone();
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public PaymentStatus getStatus() {
        return status;
    }
}
