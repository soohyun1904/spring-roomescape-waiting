package roomescape.infrastructure.toss;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.domain.payment.PaymentAlreadyProcessedException;
import roomescape.domain.payment.PaymentException;
import roomescape.domain.payment.PaymentFailedException;
import roomescape.domain.payment.PaymentInvalidRequestException;
import roomescape.domain.payment.PaymentKeyConfigurationException;
import roomescape.domain.payment.PaymentNotFoundException;
import roomescape.domain.payment.PaymentRejectedException;
import roomescape.domain.payment.PaymentRetryableException;

import java.util.stream.Stream;

class TossErrorMapperTest {
    static Stream<Arguments> errorCodeCases() {
        return Stream.of(
                Arguments.of("ALREADY_PROCESSED_PAYMENT", PaymentAlreadyProcessedException.class),
                Arguments.of("DUPLICATED_ORDER_ID", PaymentInvalidRequestException.class),
                Arguments.of("NOT_FOUND_PAYMENT_SESSION", PaymentInvalidRequestException.class),
                Arguments.of("INVALID_REQUEST", PaymentInvalidRequestException.class),
                Arguments.of("UNAUTHORIZED_KEY", PaymentKeyConfigurationException.class),
                Arguments.of("INVALID_API_KEY", PaymentKeyConfigurationException.class),
                Arguments.of("REJECT_CARD_PAYMENT", PaymentRejectedException.class),
                Arguments.of("NOT_FOUND_PAYMENT", PaymentNotFoundException.class),
                Arguments.of("FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", PaymentRetryableException.class)
        );
    }

    @ParameterizedTest
    @MethodSource("errorCodeCases")
    void 토스_에러_코드를_도메인_예외로_변환한다(String code, Class<? extends PaymentException> expected) {
        PaymentException exception = TossErrorMapper.toException(new TossErrorResponse(code, "메시지"));

        Assertions.assertThat(exception).isInstanceOf(expected);
        Assertions.assertThat(exception.getMessage()).isEqualTo("메시지");
    }

    @Test
    void 미정의_코드는_기본_예외로_폴백한다() {
        PaymentException exception = TossErrorMapper.toException(new TossErrorResponse("SOME_NEW_ERROR", "알 수 없음"));

        Assertions.assertThat(exception).isInstanceOf(PaymentFailedException.class);
    }

    @Test
    void 코드가_null이어도_기본_예외로_폴백한다() {
        PaymentException exception = TossErrorMapper.toException(new TossErrorResponse(null, null));

        Assertions.assertThat(exception).isInstanceOf(PaymentFailedException.class);
    }
}
