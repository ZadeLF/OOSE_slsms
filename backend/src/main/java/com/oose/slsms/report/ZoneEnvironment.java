package com.oose.slsms.report;

/** Per-zone noise + temperature snapshot within an {@link EnvironmentAlertReport}. */
public record ZoneEnvironment(
        String zoneId,
        String zoneName,
        double latestNoiseDb,
        double noiseThreshold,
        boolean noiseAlertActive,
        long recentNoiseAlerts,
        double latestTemperature,
        double temperatureLowerBound,
        double temperatureUpperBound,
        boolean temperatureAlertActive,
        long recentTemperatureAlerts
) {}
