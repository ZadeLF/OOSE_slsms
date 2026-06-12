package com.oose.slsms.domain.state;

import com.oose.slsms.domain.Seat;
import com.oose.slsms.exception.IllegalStateTransitionException;

import java.time.Instant;

/**
 * Reserved: a reader has booked the seat but has not yet checked in.
 * Legal transitions:
 *   - checkIn (only by reservation owner) → Occupied
 *   - release (by owner or admin) → Idle
 *   - timeout (after grace period) → Idle
 */
public final class ReservedState implements SeatState {

    @Override
    public String name() {
        return "RESERVED";
    }

    @Override
    public SeatState checkIn(Seat seat, String userId) {
        if (!userId.equals(seat.getReservationOwner())) {
            throw new IllegalStateTransitionException(
                    name(), "checkIn (reservation belongs to another user)");
        }
        seat.setCurrentUserId(userId);
        seat.setSessionStart(Instant.now());
        seat.setReservationOwner(null);
        seat.setLastChange(Instant.now());
        return SeatStates.OCCUPIED;
    }

    @Override
    public SeatState release(Seat seat, String userId) {
        seat.setReservationOwner(null);
        seat.setLastChange(Instant.now());
        return SeatStates.IDLE;
    }

    @Override
    public SeatState timeout(Seat seat) {
        seat.setReservationOwner(null);
        seat.setLastChange(Instant.now());
        return SeatStates.IDLE;
    }
}
