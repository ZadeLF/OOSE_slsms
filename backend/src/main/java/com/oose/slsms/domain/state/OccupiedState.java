package com.oose.slsms.domain.state;

import com.oose.slsms.domain.Seat;

import java.time.Instant;

/**
 * Occupied: seat is actively being used.
 * Legal transitions:
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
        seat.setTempAwaySince(Instant.now());
        seat.setLastChange(Instant.now());
        return SeatStates.TEMP_AWAY;
    }

    @Override
    public SeatState release(Seat seat, String userId) {
        seat.setCurrentUserId(null);
        seat.setSessionStart(null);
        seat.setLastChange(Instant.now());
        return SeatStates.IDLE;
    }
}
