package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.Reservations;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.Status;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final Clock clock;
    private final ReservationAssembler assembler;
    private final ReservationRepository reservationRepository;

    public ReservationService(
            Clock clock,
            ReservationAssembler assembler,
            ReservationRepository reservationRepository
    ) {
        this.clock = clock;
        this.assembler = assembler;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Reservation reserve(ReservationCreateCommand command) {
        Reservation assembled = assembler.from(command);
        Slot slot = assembled.getSlot();

        Reservations existing = reservationRepository.findBySlotId(slot.getId());
        Reservation join = existing.join(assembled);
        if (!join.isApproved()) {
            return reservationRepository.save(join);
        }

        // 슬롯을 선점한 예약은 결제 대기로 시작하고, 결제 승인이 완료되어야 확정(APPROVED)된다.
        // 주문(orderId·금액 확정)은 여기가 아니라 결제 시작 시점(POST /payments/orders)에 생성된다.
        return reservationRepository.save(join.withStatus(Status.PENDING_PAYMENT));
    }

    public Reservation find(long id) {
        Reservation reservation = reservationRepository.getById(id);
        Reservations slotReservations = reservationRepository.findBySlotId(reservation.getSlotId());
        return reservation.withRank(slotReservations.rankOf(reservation));
    }

    public Reservations findAll(String name) {
        if (name == null) {
            return reservationRepository.findAll();
        }
        return reservationRepository.findByName(name);
    }

    @Transactional
    public Reservation update(ReservationUpdateCommand command, long id) {
        Reservation existing = reservationRepository.getById(id);
        Reservation assembled = assembler.from(command);
        Slot newSlot = assembled.getSlot();

        Reservations slotReservations = reservationRepository.findBySlotId(newSlot.getId()).excluding(id);
        Reservation updated = slotReservations.join(assembled);
        reservationRepository.update(id, updated);

        boolean slotChanged = !existing.getSlotId().equals(newSlot.getId());
        if (slotChanged && existing.occupiesSlot()) {
            promoteFirstWaiting(existing.getSlotId());
        }

        return find(id);
    }

    @Transactional
    public void cancel(long reservationId, String name) {
        Reservation reservation = reservationRepository.getById(reservationId);
        LocalDateTime now = LocalDateTime.now(clock);

        reservation.validateCancellable(now);
        reservation.validateOwner(name);

        reservationRepository.deleteById(reservationId);

        if (reservation.occupiesSlot()) {
            promoteFirstWaiting(reservation.getSlotId());
        }
    }

    private void promoteFirstWaiting(Long slotId) {
        reservationRepository.findBySlotId(slotId)
                .firstWaiting()
                .ifPresent(waiting -> reservationRepository.updateStatusById(waiting.getId(), Status.APPROVED));
    }
}
