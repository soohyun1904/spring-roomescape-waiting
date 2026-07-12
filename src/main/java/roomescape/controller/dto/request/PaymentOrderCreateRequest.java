package roomescape.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PaymentOrderCreateRequest {
    @NotNull(message = "Reservation ID는 필수로 입력해야 합니다.")
    @Positive(message = "Reservation ID는 양수여야 합니다.")
    private final Long reservationId;

    public PaymentOrderCreateRequest(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getReservationId() {
        return reservationId;
    }
}
