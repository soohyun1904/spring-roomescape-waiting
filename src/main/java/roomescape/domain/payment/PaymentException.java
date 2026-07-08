package roomescape.domain.payment;

import roomescape.domain.DomainErrorCode;
import roomescape.domain.RoomEscapeException;

public abstract class PaymentException extends RoomEscapeException {
    protected PaymentException(DomainErrorCode code, String message) {
        super(code, message);
    }
}
