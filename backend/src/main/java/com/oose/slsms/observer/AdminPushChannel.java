package com.oose.slsms.observer;

import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * Concrete Observer — pushes alerts intended for human admins.
 * In production this would dispatch to a mobile push service (FCM/APNS);
 * for the demo it stores a rolling buffer the AlertController exposes.
 */
@Component
public class AdminPushChannel implements AlertObserver {

    private static final int MAX_BUFFER = 50;
    private final Deque<AlertEvent> buffer = new LinkedList<>();

    public AdminPushChannel(NoiseMonitor noiseMonitor, TemperatureMonitor temperatureMonitor) {
        noiseMonitor.register(this);
        temperatureMonitor.register(this);
    }

    @Override
    public String channelName() { return "ADMIN_PUSH"; }

    @Override
    public synchronized void onAlert(AlertEvent event) {
        buffer.addFirst(event);
        while (buffer.size() > MAX_BUFFER) buffer.removeLast();
        System.out.println("[ADMIN_PUSH] " + event.message());
    }

    public synchronized List<AlertEvent> recent() {
        return List.copyOf(buffer);
    }
}
