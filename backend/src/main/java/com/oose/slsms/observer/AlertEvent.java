package com.oose.slsms.observer;

import java.time.Instant;

/**
 * Immutable event passed from NoiseMonitor (Subject) to all AlertObservers.
 */
public record AlertEvent(
        String id,
        String zoneId,
        String type,        // NOISE_HIGH | TEMP_HIGH | TEMP_LOW
        double value,
        double threshold,
        String message,
        Instant occurredAt
) {
    public static AlertEvent noiseHigh(String zoneId, double dB, double threshold) {
        return new AlertEvent(
                "alert-" + System.currentTimeMillis(),
                zoneId,
                "NOISE_HIGH",
                dB,
                threshold,
                String.format("Zone %s noise %.1f dB exceeds threshold %.1f dB",
                        zoneId, dB, threshold),
                Instant.now()
        );
    }

    public static AlertEvent tempHigh(String zoneId, double celsius, double upperBound) {
        return new AlertEvent(
                "alert-" + System.currentTimeMillis(),
                zoneId,
                "TEMP_HIGH",
                celsius,
                upperBound,
                String.format("Zone %s temperature %.1f°C exceeds upper bound %.1f°C",
                        zoneId, celsius, upperBound),
                Instant.now()
        );
    }

    public static AlertEvent tempLow(String zoneId, double celsius, double lowerBound) {
        return new AlertEvent(
                "alert-" + System.currentTimeMillis(),
                zoneId,
                "TEMP_LOW",
                celsius,
                lowerBound,
                String.format("Zone %s temperature %.1f°C is below lower bound %.1f°C",
                        zoneId, celsius, lowerBound),
                Instant.now()
        );
    }
}
