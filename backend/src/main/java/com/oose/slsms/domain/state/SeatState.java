package com.oose.slsms.domain.state;

import com.oose.slsms.domain.Seat;
import com.oose.slsms.exception.IllegalStateTransitionException;

/**
 * State Pattern — abstract definition of all actions a Seat can receive.
 *
 * Concrete states (IdleState, ReservedState, OccupiedState, TempAwayState)
 * override only the transitions they consider legal. Default behaviour for
 * an illegal transition is to throw IllegalStateTransitionException, which
 * the GlobalExceptionHandler maps to HTTP 409 Conflict.
 *
 * Returning {@link SeatState} from each method allows the {@link Seat}
 * context to swap its current state in a single assignment, keeping the
 * delegation pattern explicit and side-effect-free at the interface level.
 */
public interface SeatState {

    String name();

    default SeatState reserve(Seat seat, String userId) {
        throw new IllegalStateTransitionException(name(), "reserve");
    }

    default SeatState checkIn(Seat seat, String userId) {
        throw new IllegalStateTransitionException(name(), "checkIn");
    }

    default SeatState leaveTemporarily(Seat seat, String userId) {
        throw new IllegalStateTransitionException(name(), "leaveTemporarily");
    }

    default SeatState comeBack(Seat seat, String userId) {
        throw new IllegalStateTransitionException(name(), "comeBack");
    }

    default SeatState release(Seat seat, String userId) {
        throw new IllegalStateTransitionException(name(), "release");
    }

    default SeatState timeout(Seat seat) {
        throw new IllegalStateTransitionException(name(), "timeout");
    }
}
