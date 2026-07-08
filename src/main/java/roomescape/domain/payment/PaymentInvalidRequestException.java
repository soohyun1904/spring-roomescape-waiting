package roomescape.domain.payment;

import roomescape.domain.DomainErrorCode;

public class PaymentInvalidRequestException extends PaymentException {
    public PaymentInvalidRequestException(String message) {
        super(DomainErrorCode.PAYMENT_INVALID_REQUEST, message);
    }
}
