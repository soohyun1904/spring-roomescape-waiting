package roomescape.common;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import roomescape.domain.DomainErrorCode;

public class DomainErrorHttpMapper {
    public static HttpStatus statusOf(final DomainErrorCode code) {
        return switch (code) {
            case RESOURCE_NOT_FOUND,
                 PAYMENT_NOT_FOUND -> HttpStatus.NOT_FOUND;

            case ALREADY_EXISTS,
                 RESOURCE_IN_USE,
                 PAYMENT_ALREADY_PROCESSED -> HttpStatus.CONFLICT;

            case INVALID_INPUT,
                 PAYMENT_AMOUNT_MISMATCH,
                 PAYMENT_INVALID_REQUEST -> HttpStatus.BAD_REQUEST;

            case PAST_DATE -> HttpStatus.UNPROCESSABLE_ENTITY;

            case FORBIDDEN -> HttpStatus.FORBIDDEN;

            case PAYMENT_REJECTED -> HttpStatus.PAYMENT_REQUIRED;

            case PAYMENT_CONFIGURATION_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;

            case PAYMENT_RETRYABLE,
                 PAYMENT_FAILED -> HttpStatus.BAD_GATEWAY;
        };
    }
}
