package com.oose.slsms.report;

import java.util.Map;

/** Per-zone breakdown within a {@link SeatUsageReport}. */
public record ZoneUsage(
        String zoneId,
        int totalSeats,
        Map<String, Long> countByState,
        double occupancyRate
) {}
