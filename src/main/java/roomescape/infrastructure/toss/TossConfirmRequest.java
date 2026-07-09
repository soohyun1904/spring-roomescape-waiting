package roomescape.infrastructure.toss;

public record TossConfirmRequest(
        String paymentKey,
        String orderId,
        Long amount
) {
}
