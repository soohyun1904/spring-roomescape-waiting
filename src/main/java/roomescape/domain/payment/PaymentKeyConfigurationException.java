package roomescape.domain.payment;

import roomescape.domain.DomainErrorCode;

public class PaymentKeyConfigurationException extends PaymentException {
    public PaymentKeyConfigurationException(String message) {
        super(DomainErrorCode.PAYMENT_CONFIGURATION_ERROR, message);
    }
}
