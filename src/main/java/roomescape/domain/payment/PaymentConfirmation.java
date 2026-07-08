package roomescape.domain.payment;

import static roomescape.domain.DomainErrorCode.INVALID_INPUT;
import static roomescape.domain.DomainPreconditions.require;
import static roomescape.domain.DomainPreconditions.requireNonBlank;
import static roomescape.domain.DomainPreconditions.requireNonNull;

public class PaymentConfirmation {
    private final String paymentKey;
    private final String orderId;
    private final Long amount;

    public PaymentConfirmation(String paymentKey, String orderId, Long amount) {
        requireNonBlank(paymentKey, INVALID_INPUT, "paymentKey는 비어있을 수 없습니다.");
        requireNonBlank(orderId, INVALID_INPUT, "orderId는 비어있을 수 없습니다.");
        requireNonNull(amount, INVALID_INPUT, "amount는 비어있을 수 없습니다.");
        require(amount > 0, INVALID_INPUT, "amount는 양수여야 합니다.");
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
