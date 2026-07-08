package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.request.PaymentConfirmRequest;
import roomescape.controller.dto.request.PaymentFailRequest;
import roomescape.controller.dto.response.PaymentConfigResponse;
import roomescape.controller.dto.response.ReservationResponse;
import roomescape.domain.reservation.Reservation;
import roomescape.service.PaymentConfirmCommand;
import roomescape.service.PaymentService;

@RestController
public class PaymentController {
    private final PaymentService paymentService;
    private final String clientKey;

    public PaymentController(
            PaymentService paymentService,
            @Value("${toss.client-key}") String clientKey
    ) {
        this.paymentService = paymentService;
        this.clientKey = clientKey;
    }

    @GetMapping("/payments/config")
    public ResponseEntity<PaymentConfigResponse> config() {
        return ResponseEntity.ok(new PaymentConfigResponse(clientKey));
    }

    @PostMapping("/payments/confirm")
    public ResponseEntity<ReservationResponse> confirm(@Valid @RequestBody PaymentConfirmRequest request) {
        Reservation reservation = paymentService.confirm(PaymentConfirmCommand.from(request));
        return ResponseEntity.ok(ReservationResponse.toDto(reservation));
    }

    @PostMapping("/payments/fail")
    public ResponseEntity<Void> fail(@RequestBody PaymentFailRequest request) {
        paymentService.cancelPending(request.getOrderId());
        return ResponseEntity.noContent().build();
    }
}
