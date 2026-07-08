package roomescape.controller.dto.response;

import roomescape.domain.payment.PaymentOrder;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Slot;

import java.time.LocalDate;

public class ReservationResponse {
    private final long id;
    private final String name;
    private final LocalDate date;
    private final String state;
    private final Long rank;
    private final ReservationTimeResponse time;
    private final ThemeResponse theme;
    private final String orderId;
    private final Long paymentAmount;

    public ReservationResponse(long id, String name, LocalDate date, String state, Long rank,
                               ReservationTimeResponse time, ThemeResponse theme,
                               String orderId, Long paymentAmount) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.state = state;
        this.rank = rank;
        this.time = time;
        this.theme = theme;
        this.orderId = orderId;
        this.paymentAmount = paymentAmount;
    }

    public static ReservationResponse toDto(Reservation reservation) {
        return toDto(reservation, null);
    }

    public static ReservationResponse toDto(Reservation reservation, PaymentOrder order) {
        Slot slot = reservation.getSlot();
        Long rank = reservation.getRank() != null ? reservation.getRank().getValue() : null;
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName().getValue(),
                slot.getDate().getDate(),
                reservation.getStatus().getKoreanName(),
                rank,
                ReservationTimeResponse.toDto(slot.getTime()),
                ThemeResponse.toDto(slot.getTheme()),
                order != null ? order.getOrderId() : null,
                order != null ? order.getAmount() : null);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getState() {
        return state;
    }

    public Long getRank() {
        return rank;
    }

    public ReservationTimeResponse getTime() {
        return time;
    }

    public ThemeResponse getTheme() {
        return theme;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getPaymentAmount() {
        return paymentAmount;
    }
}
