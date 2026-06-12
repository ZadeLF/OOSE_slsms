package com.oose.slsms.exception;

public class SeatNotFoundException extends RuntimeException {
    public SeatNotFoundException(String seatId) {
        super("Seat not found: " + seatId);
    }
}
