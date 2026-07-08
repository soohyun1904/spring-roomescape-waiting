package roomescape.domain.payment;

import roomescape.domain.DomainErrorCode;

public class PaymentFailedException extends PaymentException {
    public PaymentFailedException(String message) {
        super(DomainErrorCode.PAYMENT_FAILED, message);
    }
}
