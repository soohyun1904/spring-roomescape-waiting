package roomescape.infrastructure.toss;

import roomescape.domain.payment.PaymentAlreadyProcessedException;
import roomescape.domain.payment.PaymentException;
import roomescape.domain.payment.PaymentFailedException;
import roomescape.domain.payment.PaymentInvalidRequestException;
import roomescape.domain.payment.PaymentKeyConfigurationException;
import roomescape.domain.payment.PaymentNotFoundException;
import roomescape.domain.payment.PaymentRejectedException;
import roomescape.domain.payment.PaymentRetryableException;

// 토스 에러 코드를 도메인 예외로 변환한다. 토스 DTO는 이 패키지 밖으로 새어 나가지 않는다.
public class TossErrorMapper {
    private TossErrorMapper() {
    }

    public static PaymentException toException(TossErrorResponse error) {
        String code = error.getCode() != null ? error.getCode() : "";
        String message = error.getMessage() != null ? error.getMessage() : "결제 승인에 실패했습니다.";

        return switch (code) {
            case "ALREADY_PROCESSED_PAYMENT" -> new PaymentAlreadyProcessedException(message);
            case "DUPLICATED_ORDER_ID", "NOT_FOUND_PAYMENT_SESSION", "INVALID_REQUEST" ->
                    new PaymentInvalidRequestException(message);
            case "UNAUTHORIZED_KEY", "INVALID_API_KEY" -> new PaymentKeyConfigurationException(message);
            case "REJECT_CARD_PAYMENT" -> new PaymentRejectedException(message);
            case "NOT_FOUND_PAYMENT" -> new PaymentNotFoundException(message);
            case "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" -> new PaymentRetryableException(message);
            default -> new PaymentFailedException(message);
        };
    }
}
