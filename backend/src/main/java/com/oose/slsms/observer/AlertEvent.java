package com.oose.slsms.observer;

import java.time.Instant;

/**
 * Immutable event passed from NoiseMonitor (Subject) to all AlertObservers.
 */
public record AlertEvent(
        String id,
        String zoneId,
        String type,        // NOISE_HIGH | TEMP_HIGH | OTHER
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
}
