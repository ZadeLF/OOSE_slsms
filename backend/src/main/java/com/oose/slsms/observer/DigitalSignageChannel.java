package com.oose.slsms.observer;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Concrete Observer — drives a digital sign in the affected zone.
 * In production this would publish to a signage controller; for the demo
 * we just keep the latest message-per-zone in memory.
 */
@Component
public class DigitalSignageChannel implements AlertObserver {

    private final Map<String, String> currentMessage = new HashMap<>();

    public DigitalSignageChannel(NoiseMonitor noiseMonitor, TemperatureMonitor temperatureMonitor) {
        noiseMonitor.register(this);
        temperatureMonitor.register(this);
    }

    @Override
    public String channelName() { return "DIGITAL_SIGNAGE"; }

    @Override
    public synchronized void onAlert(AlertEvent event) {
        switch (event.type()) {
            case "NOISE_HIGH" -> currentMessage.put(event.zoneId(), "請降低音量 — Please lower your voice");
            case "TEMP_HIGH" -> currentMessage.put(event.zoneId(), "室溫過高，請留意通風 — Temperature too high");
            case "TEMP_LOW" -> currentMessage.put(event.zoneId(), "室溫過低，請注意保暖 — Temperature too low");
            default -> { /* unknown alert type — no signage message */ }
        }
        System.out.println("[DIGITAL_SIGNAGE] zone=" + event.zoneId()
                + " → " + currentMessage.get(event.zoneId()));
    }

    public synchronized String messageFor(String zoneId) {
        return currentMessage.getOrDefault(zoneId, "");
    }
}
