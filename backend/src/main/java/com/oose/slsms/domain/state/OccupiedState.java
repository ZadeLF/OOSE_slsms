package com.oose.slsms.domain.state;

import com.oose.slsms.domain.Seat;
import com.oose.slsms.exception.IllegalStateTransitionException;

import java.time.Instant;

/**
 * Occupied: seat is actively being used.
 * Legal transitions (only by the current occupant):
 *   - leaveTemporarily → TempAway
 *   - release → Idle
 */
public final class OccupiedState implements SeatState {

    @Override
    public String name() {
        return "OCCUPIED";
    }

    @Override
    public SeatState leaveTemporarily(Seat seat, String userId) {
        requireOwner(seat, userId, "leaveTemporarily");
        seat.setTempAwaySince(Instant.now());
        seat.setLastChange(Instant.now());
        return SeatStates.TEMP_AWAY;
    }

    @Override
    public SeatState release(Seat seat, String userId) {
        requireOwner(seat, userId, "release");
        seat.setCurrentUserId(null);
        seat.setSessionStart(null);
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
