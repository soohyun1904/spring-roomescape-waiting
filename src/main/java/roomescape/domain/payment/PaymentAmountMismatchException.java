package roomescape.domain.payment;

import roomescape.domain.DomainErrorCode;

public class PaymentAmountMismatchException extends PaymentException {
    public PaymentAmountMismatchException(String message) {
        super(DomainErrorCode.PAYMENT_AMOUNT_MISMATCH, message);
    }
}
