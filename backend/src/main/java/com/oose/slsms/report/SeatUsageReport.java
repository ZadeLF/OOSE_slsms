package com.oose.slsms.report;

import java.util.List;
import java.util.Map;

/** Result of {@link SeatUsageReportStrategy}. */
public record SeatUsageReport(
        String type,
        String title,
        int totalSeats,
        Map<String, Long> countByState,
        double occupancyRate,
        List<ZoneUsage> byZone
) {}
