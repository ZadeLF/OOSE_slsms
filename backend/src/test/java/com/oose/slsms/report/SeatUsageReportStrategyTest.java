package com.oose.slsms.report;

import com.oose.slsms.domain.Seat;
import com.oose.slsms.repository.SeatRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SeatUsageReportStrategy}.
 * Verifies the strategy's calculation against a known set of seat states.
 */
class SeatUsageReportStrategyTest {

    @Test
    void computes_overall_and_per_zone_occupancy() {
        SeatRepository repo = new SeatRepository();

        // Zone A: 2 occupied, 1 idle (3 seats)
        Seat a1 = new Seat("A1", "A", 0, 0);
        a1.checkIn("user-1");
        Seat a2 = new Seat("A2", "A", 0, 1);
        a2.checkIn("user-2");
        Seat a3 = new Seat("A3", "A", 0, 2); // idle
        repo.save(a1);
        repo.save(a2);
        repo.save(a3);

        // Zone B: 1 temp-away, 1 reserved (2 seats)
        Seat b1 = new Seat("B1", "B", 1, 0);
        b1.checkIn("user-3");
        b1.leaveTemporarily("user-3");
        Seat b2 = new Seat("B2", "B", 1, 1);
        b2.reserve("user-4");
        repo.save(b1);
        repo.save(b2);

        SeatUsageReportStrategy strategy = new SeatUsageReportStrategy(repo);

        assertEquals("seat-usage", strategy.type());

        SeatUsageReport report = strategy.generate();

        assertEquals(5, report.totalSeats());
        // Overall: OCCUPIED=2, TEMP_AWAY=1 → 3 in-use out of 5
        assertEquals(3L, report.countByState().get("OCCUPIED") + report.countByState().get("TEMP_AWAY"));
        assertEquals(3.0 / 5.0, report.occupancyRate(), 1e-9);

        assertEquals(2, report.byZone().size());

        ZoneUsage zoneA = report.byZone().stream()
                .filter(z -> z.zoneId().equals("A")).findFirst().orElseThrow();
        assertEquals(3, zoneA.totalSeats());
        assertEquals(2L, zoneA.countByState().get("OCCUPIED"));
        assertEquals(1L, zoneA.countByState().get("IDLE"));
        assertEquals(2.0 / 3.0, zoneA.occupancyRate(), 1e-9);

        ZoneUsage zoneB = report.byZone().stream()
                .filter(z -> z.zoneId().equals("B")).findFirst().orElseThrow();
        assertEquals(2, zoneB.totalSeats());
        assertEquals(1L, zoneB.countByState().get("TEMP_AWAY"));
        assertEquals(1L, zoneB.countByState().get("RESERVED"));
        assertEquals(0.5, zoneB.occupancyRate(), 1e-9);
    }

    @Test
    void empty_repository_yields_zero_seats_and_zero_rate() {
        SeatRepository repo = new SeatRepository();
        SeatUsageReportStrategy strategy = new SeatUsageReportStrategy(repo);

        SeatUsageReport report = strategy.generate();

        assertEquals(0, report.totalSeats());
        assertEquals(0.0, report.occupancyRate());
        assertTrue(report.byZone().isEmpty());
    }
}
