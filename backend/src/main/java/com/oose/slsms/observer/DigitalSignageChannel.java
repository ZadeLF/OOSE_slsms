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

    public DigitalSignageChannel(NoiseMonitor monitor) {
        monitor.register(this);
    }

    @Override
    public String channelName() { return "DIGITAL_SIGNAGE"; }

    @Override
    public synchronized void onAlert(AlertEvent event) {
        if ("NOISE_HIGH".equals(event.type())) {
            currentMessage.put(event.zoneId(), "請降低音量 — Please lower your voice");
        }
        System.out.println("[DIGITAL_SIGNAGE] zone=" + event.zoneId()
                + " → " + currentMessage.get(event.zoneId()));
    }

    public synchronized String messageFor(String zoneId) {
        return currentMessage.getOrDefault(zoneId, "");
    }
}
