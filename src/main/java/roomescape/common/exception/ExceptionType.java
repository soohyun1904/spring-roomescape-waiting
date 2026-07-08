package roomescape.common.exception;

import org.springframework.http.HttpStatus;
import roomescape.domain.DomainErrorCode;

public enum ExceptionType {
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, DomainErrorCode.RESOURCE_NOT_FOUND),
    ALREADY_EXISTS(HttpStatus.CONFLICT, DomainErrorCode.ALREADY_EXISTS),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, DomainErrorCode.INVALID_INPUT),
    PAST_DATE(HttpStatus.UNPROCESSABLE_ENTITY, DomainErrorCode.PAST_DATE),
    FORBIDDEN(HttpStatus.UNAUTHORIZED, DomainErrorCode.FORBIDDEN),
    RESOURCE_IN_USE(HttpStatus.CONFLICT, DomainErrorCode.RESOURCE_IN_USE),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, DomainErrorCode.PAYMENT_AMOUNT_MISMATCH),
    PAYMENT_ALREADY_PROCESSED(HttpStatus.CONFLICT, DomainErrorCode.PAYMENT_ALREADY_PROCESSED),
    PAYMENT_INVALID_REQUEST(HttpStatus.BAD_REQUEST, DomainErrorCode.PAYMENT_INVALID_REQUEST),
    PAYMENT_REJECTED(HttpStatus.PAYMENT_REQUIRED, DomainErrorCode.PAYMENT_REJECTED),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, DomainErrorCode.PAYMENT_NOT_FOUND),
    PAYMENT_CONFIGURATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, DomainErrorCode.PAYMENT_CONFIGURATION_ERROR),
    PAYMENT_RETRYABLE(HttpStatus.BAD_GATEWAY, DomainErrorCode.PAYMENT_RETRYABLE),
    PAYMENT_FAILED(HttpStatus.BAD_GATEWAY, DomainErrorCode.PAYMENT_FAILED);

    private final HttpStatus status;
    private final DomainErrorCode domainErrorCode;

    ExceptionType(HttpStatus status, DomainErrorCode domainErrorCode) {
        this.status = status;
        this.domainErrorCode = domainErrorCode;
    }

    public static HttpStatus resolveStatus(DomainErrorCode code) {
        return switch (code) {
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case INVALID_INPUT -> HttpStatus.BAD_REQUEST;
            case PAST_DATE -> HttpStatus.UNPROCESSABLE_ENTITY;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case RESOURCE_IN_USE -> HttpStatus.CONFLICT;
            case PAYMENT_AMOUNT_MISMATCH -> HttpStatus.BAD_REQUEST;
            case PAYMENT_ALREADY_PROCESSED -> HttpStatus.CONFLICT;
            case PAYMENT_INVALID_REQUEST -> HttpStatus.BAD_REQUEST;
            case PAYMENT_REJECTED -> HttpStatus.PAYMENT_REQUIRED;
            case PAYMENT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case PAYMENT_CONFIGURATION_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            case PAYMENT_RETRYABLE, PAYMENT_FAILED -> HttpStatus.BAD_GATEWAY;
        };
    }

    public HttpStatus getStatus() {
        return status;
    }
}
