package roomescape.infrastructure.toss;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// tolerant reader: 토스 응답의 모르는 필드는 무시하고 필요한 것만 읽는다.
@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentResponse (
        String paymentKey,
        String orderId,
        Long totalAmount,
        String status
) {
}
