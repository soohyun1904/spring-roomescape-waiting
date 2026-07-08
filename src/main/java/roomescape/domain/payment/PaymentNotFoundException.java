package roomescape.domain.payment;

import roomescape.domain.DomainErrorCode;

public class PaymentNotFoundException extends PaymentException {
    public PaymentNotFoundException(String message) {
        super(DomainErrorCode.PAYMENT_NOT_FOUND, message);
    }
}
