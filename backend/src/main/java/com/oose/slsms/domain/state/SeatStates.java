package com.oose.slsms.domain.state;

/**
 * Flyweight holder for state singletons.
 *
 * Concrete state objects are stateless (all per-seat data lives on the Seat
 * context itself), so a single instance per state class is shared across all
 * seats. This combines the State pattern with the Flyweight pattern — saving
 * thousands of object allocations once the system scales beyond demo size.
 */
public final class SeatStates {

    public static final SeatState IDLE      = new IdleState();
    public static final SeatState RESERVED  = new ReservedState();
    public static final SeatState OCCUPIED  = new OccupiedState();
    public static final SeatState TEMP_AWAY = new TempAwayState();

    private SeatStates() {}

    public static SeatState byName(String name) {
        return switch (name) {
            case "IDLE"      -> IDLE;
            case "RESERVED"  -> RESERVED;
            case "OCCUPIED"  -> OCCUPIED;
            case "TEMP_AWAY" -> TEMP_AWAY;
            default -> throw new IllegalArgumentException("Unknown state: " + name);
        };
    }
}
