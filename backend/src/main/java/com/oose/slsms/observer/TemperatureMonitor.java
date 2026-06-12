package com.oose.slsms.observer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observer Pattern — concrete Subject for temperature readings.
 *
 * Mirrors {@link NoiseMonitor}'s edge-triggered design, but for an
 * environmental indicator with both an upper and a lower bound: an
 * {@link AlertEvent} of type {@code TEMP_HIGH} or {@code TEMP_LOW} is
 * dispatched only when a zone transitions from "within range" to
 * "out of range" in that direction. Returning to within range clears the
 * active alert without firing a "cleared" event, same as NoiseMonitor.
 *
 * Demonstrates that the existing Observer architecture (same
 * {@link AlertObserver} interface, same channels) extends to a second
 * sensor type without any change to {@link AdminPushChannel} or
 * {@link DigitalSignageChannel} beyond handling the new event types.
 */
@Component
public class TemperatureMonitor {

    private static final double DEFAULT_LOWER_BOUND = 18.0;
    private static final double DEFAULT_UPPER_BOUND = 28.0;

    private final List<AlertObserver> observers = new CopyOnWriteArrayList<>();

    /** Per-zone configuration (defaults to 18~28 °C). */
    private final Map<String, Double> lowerBounds = new HashMap<>();
    private final Map<String, Double> upperBounds = new HashMap<>();

    /** Last-known reading per zone. */
    private final Map<String, Double> latest = new HashMap<>();

    /** Which bound (if any) each zone is currently out of: "TEMP_HIGH" | "TEMP_LOW" | null. */
    private final Map<String, String> activeAlertType = new HashMap<>();

    public synchronized void register(AlertObserver observer) {
        observers.add(observer);
    }

    public synchronized void unregister(AlertObserver observer) {
        observers.remove(observer);
    }

    public synchronized List<AlertObserver> observers() {
        return new ArrayList<>(observers);
    }

    public synchronized void setBounds(String zoneId, double lower, double upper) {
        lowerBounds.put(zoneId, lower);
        upperBounds.put(zoneId, upper);
    }

    public synchronized double getLowerBound(String zoneId) {
        return lowerBounds.getOrDefault(zoneId, DEFAULT_LOWER_BOUND);
    }

    public synchronized double getUpperBound(String zoneId) {
        return upperBounds.getOrDefault(zoneId, DEFAULT_UPPER_BOUND);
    }

    public synchronized double getLatest(String zoneId) {
        return latest.getOrDefault(zoneId, 0.0);
    }

    public synchronized boolean isAlertActive(String zoneId) {
        return activeAlertType.get(zoneId) != null;
    }

    /**
     * Push a temperature reading into the monitor.
     * Edge-triggered in both directions: an event fires only when the zone
     * newly goes above the upper bound or below the lower bound. Returning
     * to within [lower, upper] clears the active alert.
     */
    public void recordTemperature(String zoneId, double celsius) {
        AlertEvent event = null;
        synchronized (this) {
            latest.put(zoneId, celsius);
            double lower = getLowerBound(zoneId);
            double upper = getUpperBound(zoneId);
            String active = activeAlertType.get(zoneId);

            if (celsius > upper) {
                if (!"TEMP_HIGH".equals(active)) {
                    event = AlertEvent.tempHigh(zoneId, celsius, upper);
                    activeAlertType.put(zoneId, "TEMP_HIGH");
                }
            } else if (celsius < lower) {
                if (!"TEMP_LOW".equals(active)) {
                    event = AlertEvent.tempLow(zoneId, celsius, lower);
                    activeAlertType.put(zoneId, "TEMP_LOW");
                }
            } else {
                activeAlertType.put(zoneId, null);
            }
        }
        if (event != null) notifyAll(event);
    }

    private void notifyAll(AlertEvent event) {
        for (AlertObserver observer : observers) {
            try {
                observer.onAlert(event);
            } catch (Exception e) {
                System.err.println("[TemperatureMonitor] observer "
                        + observer.channelName() + " failed: " + e.getMessage());
            }
        }
    }
}
