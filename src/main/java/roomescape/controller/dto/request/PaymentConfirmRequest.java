package roomescape.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PaymentConfirmRequest {
    @NotBlank(message = "paymentKey는 필수로 입력해야 합니다.")
    private final String paymentKey;

    @NotBlank(message = "orderId는 필수로 입력해야 합니다.")
    private final String orderId;

    @NotNull(message = "amount는 필수로 입력해야 합니다.")
    @Positive(message = "amount는 양수여야 합니다.")
    private final Long amount;

    public PaymentConfirmRequest(String paymentKey, String orderId, Long amount) {
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
