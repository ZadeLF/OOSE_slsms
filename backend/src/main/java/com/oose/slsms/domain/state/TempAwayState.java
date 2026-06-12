package com.oose.slsms.domain.state;

import com.oose.slsms.domain.Seat;
import com.oose.slsms.exception.IllegalStateTransitionException;

import java.time.Instant;

/**
 * TempAway: occupant temporarily left (max 30 minutes).
 * Legal transitions (comeBack/release only by the current occupant):
 *   - comeBack → Occupied
 *   - release → Idle
 *   - timeout (over 30 min) → Idle
 */
public final class TempAwayState implements SeatState {

    @Override
    public String name() {
        return "TEMP_AWAY";
    }

    @Override
    public SeatState comeBack(Seat seat, String userId) {
        requireOwner(seat, userId, "comeBack");
        seat.setTempAwaySince(null);
        seat.setLastChange(Instant.now());
        return SeatStates.OCCUPIED;
    }

    @Override
    public SeatState release(Seat seat, String userId) {
        requireOwner(seat, userId, "release");
        seat.setCurrentUserId(null);
        seat.setSessionStart(null);
        seat.setTempAwaySince(null);
        seat.setLastChange(Instant.now());
        return SeatStates.IDLE;
    }

    @Override
    public SeatState timeout(Seat seat) {
        seat.setCurrentUserId(null);
        seat.setSessionStart(null);
        seat.setTempAwaySince(null);
        seat.setLastChange(Instant.now());
        return SeatStates.IDLE;
    }

    private void requireOwner(Seat seat, String userId, String action) {
        if (!userId.equals(seat.getCurrentUserId())) {
            throw new IllegalStateTransitionException(
                    name(), action + " (seat is occupied by another user)");
        }
    }
}
