package roomescape.service;

import roomescape.domain.payment.PaymentOrder;
import roomescape.domain.reservation.Reservation;

public class ReservationWithOrder {
    private final Reservation reservation;
    private final PaymentOrder order;

    public ReservationWithOrder(Reservation reservation, PaymentOrder order) {
        this.reservation = reservation;
        this.order = order;
    }

    public static ReservationWithOrder withoutOrder(Reservation reservation) {
        return new ReservationWithOrder(reservation, null);
    }

    public Reservation getReservation() {
        return reservation;
    }

    public PaymentOrder getOrder() {
        return order;
    }
}
