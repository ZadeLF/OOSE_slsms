package com.oose.slsms.dto;

import com.oose.slsms.domain.Seat;

import java.time.Instant;

/**
 * DTO returned by the seat REST endpoints.
 */
public record SeatDto(
        String id,
        String zoneId,
        int gridRow,
        int gridCol,
        String state,
        String currentUserId,
        String reservationOwner,
        Instant lastChange
) {
    public static SeatDto from(Seat s) {
        return new SeatDto(
                s.getId(),
                s.getZoneId(),
                s.getGridRow(),
                s.getGridCol(),
                s.getStateName(),
                s.getCurrentUserId(),
                s.getReservationOwner(),
                s.getLastChange()
        );
    }
}
