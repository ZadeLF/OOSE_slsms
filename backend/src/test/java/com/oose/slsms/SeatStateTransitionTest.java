package com.oose.slsms;

import com.oose.slsms.domain.Seat;
import com.oose.slsms.exception.IllegalStateTransitionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Seat State machine.
 * Maps directly to TC-02 ~ TC-05 in the SRS Test Document.
 */
class SeatStateTransitionTest {

    private Seat newSeat() { return new Seat("A1", "A", 0, 0); }

    // TC-02: Idle → checkIn → Occupied
    @Test
    void idle_to_occupied_via_checkin() {
        Seat seat = newSeat();
        assertEquals("IDLE", seat.getStateName());
        seat.checkIn("user-1");
        assertEquals("OCCUPIED", seat.getStateName());
        assertEquals("user-1", seat.getCurrentUserId());
    }

    // TC-03: Occupied → checkIn() should throw 409
    @Test
    void occupied_cannot_checkin_again() {
        Seat seat = newSeat();
        seat.checkIn("user-1");
        assertThrows(IllegalStateTransitionException.class,
                () -> seat.checkIn("user-2"));
    }

    // Reservation owner check
    @Test
    void reserved_seat_rejects_checkin_by_other_user() {
        Seat seat = newSeat();
        seat.reserve("user-1");
        assertEquals("RESERVED", seat.getStateName());
        assertThrows(IllegalStateTransitionException.class,
                () -> seat.checkIn("user-2"));
    }

    @Test
    void reserved_seat_accepts_checkin_by_owner() {
        Seat seat = newSeat();
        seat.reserve("user-1");
        seat.checkIn("user-1");
        assertEquals("OCCUPIED", seat.getStateName());
        assertNull(seat.getReservationOwner());
    }

    // TC-04: Reserved → timeout → Idle
    @Test
    void reserved_timeout_returns_to_idle() {
        Seat seat = newSeat();
        seat.reserve("user-1");
        seat.timeout();
        assertEquals("IDLE", seat.getStateName());
        assertNull(seat.getReservationOwner());
    }

    // TC-05: Occupied → leaveTemp → TempAway → comeBack → Occupied
    @Test
    void occupied_leave_temp_come_back_cycle() {
        Seat seat = newSeat();
        seat.checkIn("user-1");
        seat.leaveTemporarily("user-1");
        assertEquals("TEMP_AWAY", seat.getStateName());
        assertNotNull(seat.getTempAwaySince());
        seat.comeBack("user-1");
        assertEquals("OCCUPIED", seat.getStateName());
        assertNull(seat.getTempAwaySince());
    }

    // TempAway timeout
    @Test
    void temp_away_timeout_resets_to_idle() {
        Seat seat = newSeat();
        seat.checkIn("user-1");
        seat.leaveTemporarily("user-1");
        seat.timeout();
        assertEquals("IDLE", seat.getStateName());
        assertNull(seat.getCurrentUserId());
    }

    // Idle cannot be released
    @Test
    void idle_cannot_be_released() {
        Seat seat = newSeat();
        assertThrows(IllegalStateTransitionException.class,
                () -> seat.release("user-1"));
    }

    // Idle cannot leaveTemp
    @Test
    void idle_cannot_leave_temp() {
        Seat seat = newSeat();
        assertThrows(IllegalStateTransitionException.class,
                () -> seat.leaveTemporarily("user-1"));
    }

    // Reserved cannot be reserved again
    @Test
    void reserved_cannot_be_reserved_again() {
        Seat seat = newSeat();
        seat.reserve("user-1");
        assertThrows(IllegalStateTransitionException.class,
                () -> seat.reserve("user-2"));
    }
}
