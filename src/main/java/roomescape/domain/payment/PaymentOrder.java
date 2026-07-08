package roomescape.domain.payment;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import static roomescape.domain.DomainErrorCode.INVALID_INPUT;
import static roomescape.domain.DomainPreconditions.require;
import static roomescape.domain.DomainPreconditions.requireNonBlank;
import static roomescape.domain.DomainPreconditions.requireNonNull;

public class PaymentOrder {
    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{6,64}$");

    private final Long id;
    private final String orderId;
    private final Long reservationId;
    private final Long amount;
    private final String paymentKey;

    private PaymentOrder(Long id, String orderId, Long reservationId, Long amount, String paymentKey) {
        requireNonBlank(orderId, INVALID_INPUT, "주문 번호는 비어있을 수 없습니다.");
        require(ORDER_ID_PATTERN.matcher(orderId).matches(), INVALID_INPUT, "주문 번호 형식이 올바르지 않습니다: " + orderId);
        requireNonNull(reservationId, INVALID_INPUT, "주문의 예약 ID는 비어있을 수 없습니다.");
        requireNonNull(amount, INVALID_INPUT, "주문 금액은 비어있을 수 없습니다.");
        require(amount > 0, INVALID_INPUT, "주문 금액은 양수여야 합니다.");
        this.id = id;
        this.orderId = orderId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.paymentKey = paymentKey;
    }

    public static PaymentOrder create(Long reservationId, Long amount) {
        return new PaymentOrder(null, "order-" + UUID.randomUUID(), reservationId, amount, null);
    }

    public static PaymentOrder load(Long id, String orderId, Long reservationId, Long amount, String paymentKey) {
        return new PaymentOrder(id, orderId, reservationId, amount, paymentKey);
    }

    public PaymentOrder withId(Long generatedKey) {
        return new PaymentOrder(generatedKey, orderId, reservationId, amount, paymentKey);
    }

    public PaymentOrder withPaymentKey(String paymentKey) {
        return new PaymentOrder(id, orderId, reservationId, amount, paymentKey);
    }

    public boolean isSameAmount(Long other) {
        return amount.equals(other);
    }

    public Long getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PaymentOrder that = (PaymentOrder) o;
        return Objects.equals(orderId, that.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(orderId);
    }
}
