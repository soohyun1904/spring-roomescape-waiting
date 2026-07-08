package roomescape.domain.payment;

import java.util.Arrays;

public enum PaymentStatus {
    READY,
    IN_PROGRESS,
    WAITING_FOR_DEPOSIT,
    DONE,
    CANCELED,
    PARTIAL_CANCELED,
    ABORTED,
    EXPIRED,
    UNKNOWN;

    public static PaymentStatus from(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public boolean isDone() {
        return this == DONE;
    }
}
