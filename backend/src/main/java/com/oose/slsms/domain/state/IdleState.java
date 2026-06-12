package com.oose.slsms.domain.state;

import com.oose.slsms.domain.Seat;

import java.time.Instant;

/**
 * Idle: seat is empty and available.
 * Legal transitions: reserve → Reserved, checkIn → Occupied.
 */
public final class IdleState implements SeatState {

    @Override
    public String name() {
        return "IDLE";
    }

    @Override
    public SeatState reserve(Seat seat, String userId) {
        seat.setReservationOwner(userId);
        seat.setLastChange(Instant.now());
        return SeatStates.RESERVED;
    }

    @Override
    public SeatState checkIn(Seat seat, String userId) {
        seat.setCurrentUserId(userId);
        seat.setSessionStart(Instant.now());
        seat.setLastChange(Instant.now());
        return SeatStates.OCCUPIED;
    }
}
