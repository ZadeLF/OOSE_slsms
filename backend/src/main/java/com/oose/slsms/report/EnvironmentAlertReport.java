package com.oose.slsms.report;

import java.util.List;

/** Result of {@link EnvironmentAlertReportStrategy}. */
public record EnvironmentAlertReport(
        String type,
        String title,
        int totalRecentAlerts,
        List<ZoneEnvironment> byZone
) {}
