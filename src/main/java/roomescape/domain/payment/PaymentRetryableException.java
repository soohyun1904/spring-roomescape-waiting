package roomescape.domain.payment;

import roomescape.domain.DomainErrorCode;

/**
 * 외부 결제 시스템의 일시적 오류로, 재시도하면 성공할 수 있는 실패를 표시한다.
 * 실제 재시도 로직은 이번 범위가 아니며, 재시도 대상임을 타입으로 구분만 해둔다.
 */
public class PaymentRetryableException extends PaymentException {
    public PaymentRetryableException(String message) {
        super(DomainErrorCode.PAYMENT_RETRYABLE, message);
    }
}
