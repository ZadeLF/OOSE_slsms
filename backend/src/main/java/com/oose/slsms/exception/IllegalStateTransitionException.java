package com.oose.slsms.exception;

/**
 * Thrown when an action is called on a SeatState that does not allow it.
 * Mapped to HTTP 409 Conflict by GlobalExceptionHandler.
 */
public class IllegalStateTransitionException extends RuntimeException {
    public IllegalStateTransitionException(String currentState, String action) {
        super(String.format("Action '%s' is not allowed in state '%s'", action, currentState));
    }
}
