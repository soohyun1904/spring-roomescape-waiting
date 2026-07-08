package roomescape.domain.payment;

import roomescape.domain.DomainErrorCode;

public class PaymentRejectedException extends PaymentException {
    public PaymentRejectedException(String message) {
        super(DomainErrorCode.PAYMENT_REJECTED, message);
    }
}
