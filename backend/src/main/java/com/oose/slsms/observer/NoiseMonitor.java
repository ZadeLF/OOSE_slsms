package com.oose.slsms.observer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observer Pattern — concrete Subject for noise readings.
 *
 * Receives raw sensor values via {@link #recordNoise} and notifies all
 * registered {@link AlertObserver}s when a zone crosses its threshold
 * upward (going from "below" to "above"). This edge-trigger behaviour
 * prevents alert flooding when noise hovers near the threshold.
 */
@Component
public class NoiseMonitor {

    private final List<AlertObserver> observers = new CopyOnWriteArrayList<>();

    /** Per-zone configuration (defaults to 65 dB). */
    private final Map<String, Double> thresholds = new HashMap<>();

    /** Last-known reading per zone. */
    private final Map<String, Double> latest = new HashMap<>();

    /** Whether each zone is currently in an "alert" state. */
    private final Map<String, Boolean> alertActive = new HashMap<>();

    public synchronized void register(AlertObserver observer) {
        observers.add(observer);
    }

    public synchronized void unregister(AlertObserver observer) {
        observers.remove(observer);
    }

    public synchronized List<AlertObserver> observers() {
        return new ArrayList<>(observers);
    }

    public synchronized void setThreshold(String zoneId, double dB) {
        thresholds.put(zoneId, dB);
    }

    public synchronized double getThreshold(String zoneId) {
        return thresholds.getOrDefault(zoneId, 65.0);
    }

    public synchronized double getLatest(String zoneId) {
        return latest.getOrDefault(zoneId, 0.0);
    }

    public synchronized boolean isAlertActive(String zoneId) {
        return alertActive.getOrDefault(zoneId, false);
    }

    /**
     * Push a sensor reading into the monitor.
     * Edge-triggered: an event is dispatched only when the zone transitions
     * from "below threshold" to "above threshold" (and again on the falling
     * edge as a "cleared" event in a future iteration).
     */
    public void recordNoise(String zoneId, double dB) {
        AlertEvent event = null;
        synchronized (this) {
            latest.put(zoneId, dB);
            double threshold = getThreshold(zoneId);
            boolean wasActive = alertActive.getOrDefault(zoneId, false);
            boolean isActive = dB > threshold;

            if (isActive && !wasActive) {
                event = AlertEvent.noiseHigh(zoneId, dB, threshold);
                alertActive.put(zoneId, true);
            } else if (!isActive && wasActive) {
                alertActive.put(zoneId, false);
            }
        }
        if (event != null) notifyAll(event);
    }

    private void notifyAll(AlertEvent event) {
        for (AlertObserver observer : observers) {
            try {
                observer.onAlert(event);
            } catch (Exception e) {
                System.err.println("[NoiseMonitor] observer "
                        + observer.channelName() + " failed: " + e.getMessage());
            }
        }
    }
}
