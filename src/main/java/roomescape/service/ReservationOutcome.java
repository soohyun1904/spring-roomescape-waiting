package roomescape.service;

import roomescape.domain.payment.PaymentOrder;
import roomescape.domain.reservation.Reservation;

/**
 * 예약 신청의 결과는 정확히 두 갈래다.
 * 슬롯을 선점해 결제가 필요한 경우(PaymentRequired)에는 주문이 반드시 있고,
 * 대기로 합류한 경우(Joined)에는 주문이 존재할 수 없다 — null 대신 타입으로 표현한다.
 */
public sealed interface ReservationOutcome {
    Reservation reservation();

    record PaymentRequired(Reservation reservation, PaymentOrder order) implements ReservationOutcome {
    }

    record Joined(Reservation reservation) implements ReservationOutcome {
    }
}
