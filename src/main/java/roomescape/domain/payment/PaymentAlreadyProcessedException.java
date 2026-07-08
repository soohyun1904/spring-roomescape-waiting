package roomescape.domain.payment;

import roomescape.domain.DomainErrorCode;

public class PaymentAlreadyProcessedException extends PaymentException {
    public PaymentAlreadyProcessedException(String message) {
        super(DomainErrorCode.PAYMENT_ALREADY_PROCESSED, message);
    }
}
