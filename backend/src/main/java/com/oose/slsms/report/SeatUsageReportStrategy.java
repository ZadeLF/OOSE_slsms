package com.oose.slsms.report;

import com.oose.slsms.domain.Seat;
import com.oose.slsms.repository.SeatRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Strategy Pattern — concrete report: seat usage / occupancy rate.
 *
 * Computed entirely from {@link SeatRepository}'s current in-memory seat
 * states (no persistence): for the whole floor and per zone, how many seats
 * are in each {@code SeatState} and what fraction are currently "in use"
 * (OCCUPIED or TEMP_AWAY).
 */
@Component
public class SeatUsageReportStrategy implements ReportStrategy {

    private static final List<String> STATE_NAMES = List.of("IDLE", "RESERVED", "OCCUPIED", "TEMP_AWAY");

    private final SeatRepository repository;

    public SeatUsageReportStrategy(SeatRepository repository) {
        this.repository = repository;
    }

    @Override
    public String type() {
        return "seat-usage";
    }

    @Override
    public String title() {
        return "座位使用率報表";
    }

    @Override
    public SeatUsageReport generate() {
        List<Seat> seats = repository.findAllSeats();

        Map<String, Long> overall = countByState(seats);
        double overallRate = occupancyRate(overall, seats.size());

        Map<String, List<Seat>> byZone = seats.stream()
                .collect(Collectors.groupingBy(Seat::getZoneId));

        List<ZoneUsage> zoneUsages = byZone.entrySet().stream()
                .map(e -> {
                    List<Seat> zoneSeats = e.getValue();
                    Map<String, Long> zoneCounts = countByState(zoneSeats);
                    return new ZoneUsage(
                            e.getKey(),
                            zoneSeats.size(),
                            zoneCounts,
                            occupancyRate(zoneCounts, zoneSeats.size()));
                })
                .sorted(Comparator.comparing(ZoneUsage::zoneId))
                .toList();

        return new SeatUsageReport(type(), title(), seats.size(), overall, overallRate, zoneUsages);
    }

    private Map<String, Long> countByState(List<Seat> seats) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String state : STATE_NAMES) counts.put(state, 0L);
        for (Seat seat : seats) {
            counts.merge(seat.getStateName(), 1L, Long::sum);
        }
        return counts;
    }

    private double occupancyRate(Map<String, Long> counts, int total) {
        if (total == 0) return 0.0;
        long inUse = counts.getOrDefault("OCCUPIED", 0L) + counts.getOrDefault("TEMP_AWAY", 0L);
        return (double) inUse / total;
    }
}
