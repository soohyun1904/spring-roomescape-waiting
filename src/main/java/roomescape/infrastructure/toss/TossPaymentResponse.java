package roomescape.infrastructure.toss;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// tolerant reader: 토스 응답의 모르는 필드는 무시하고 필요한 것만 읽는다.
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossPaymentResponse {
    private final String paymentKey;
    private final String orderId;
    private final Long totalAmount;
    private final String status;

    @JsonCreator
    public TossPaymentResponse(
            @JsonProperty("paymentKey") String paymentKey,
            @JsonProperty("orderId") String orderId,
            @JsonProperty("totalAmount") Long totalAmount,
            @JsonProperty("status") String status) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.status = status;
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

    public String getStatus() {
        return status;
    }
}
