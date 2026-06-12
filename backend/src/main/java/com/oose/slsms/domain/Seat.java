package com.oose.slsms.domain;

import com.oose.slsms.domain.state.SeatState;
import com.oose.slsms.domain.state.SeatStates;

import java.time.Instant;
import java.util.Objects;

/**
 * Seat — State Pattern context.
 *
 * Holds a reference to its current {@link SeatState} and delegates all
 * action requests to it. The state object itself decides whether the action
 * is legal and returns the new state, which we assign back to {@link #state}.
 *
 * Identity is the seat ID (e.g. "A1", "B3"); zoneId locates the seat for
 * Observer notifications; the rest are session-related fields.
 */
public class Seat {

    private final String id;
    private final String zoneId;
    private final int gridRow;
    private final int gridCol;

    private SeatState state;
    private String currentUserId;
    private String reservationOwner;
    private Instant sessionStart;
    private Instant tempAwaySince;
    private Instant lastChange;

    public Seat(String id, String zoneId, int gridRow, int gridCol) {
        this.id = id;
        this.zoneId = zoneId;
        this.gridRow = gridRow;
        this.gridCol = gridCol;
        this.state = SeatStates.IDLE;
        this.lastChange = Instant.now();
    }

    // ----- State delegation (the heart of the pattern) -----

    public synchronized void reserve(String userId) {
        this.state = state.reserve(this, userId);
    }

    public synchronized void checkIn(String userId) {
        this.state = state.checkIn(this, userId);
    }

    public synchronized void leaveTemporarily(String userId) {
        this.state = state.leaveTemporarily(this, userId);
    }

    public synchronized void comeBack(String userId) {
        this.state = state.comeBack(this, userId);
    }

    public synchronized void release(String userId) {
        this.state = state.release(this, userId);
    }

    public synchronized void timeout() {
        this.state = state.timeout(this);
    }

    // ----- Getters / setters used by states (package-friendly) -----

    public String getId() { return id; }
    public String getZoneId() { return zoneId; }
    public int getGridRow() { return gridRow; }
    public int getGridCol() { return gridCol; }
    public SeatState getState() { return state; }
    public String getStateName() { return state.name(); }

    public String getCurrentUserId() { return currentUserId; }
    public void setCurrentUserId(String currentUserId) { this.currentUserId = currentUserId; }

    public String getReservationOwner() { return reservationOwner; }
    public void setReservationOwner(String reservationOwner) { this.reservationOwner = reservationOwner; }

    public Instant getSessionStart() { return sessionStart; }
    public void setSessionStart(Instant sessionStart) { this.sessionStart = sessionStart; }

    public Instant getTempAwaySince() { return tempAwaySince; }
    public void setTempAwaySince(Instant tempAwaySince) { this.tempAwaySince = tempAwaySince; }

    public Instant getLastChange() { return lastChange; }
    public void setLastChange(Instant lastChange) { this.lastChange = lastChange; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Seat seat)) return false;
        return Objects.equals(id, seat.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
