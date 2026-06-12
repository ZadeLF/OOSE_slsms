package com.oose.slsms;

import com.oose.slsms.observer.AlertEvent;
import com.oose.slsms.observer.AlertObserver;
import com.oose.slsms.observer.NoiseMonitor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the NoiseMonitor (Subject) + AlertObserver pair.
 * Maps to TC-06 in the SRS Test Document.
 */
class NoiseObserverTest {

    /** A capturing test-double observer. */
    static class CapturingObserver implements AlertObserver {
        final List<AlertEvent> received = new ArrayList<>();
        @Override public String channelName() { return "TEST"; }
        @Override public void onAlert(AlertEvent event) { received.add(event); }
    }

    @Test
    void crossing_threshold_upward_triggers_alert() {
        NoiseMonitor monitor = new NoiseMonitor();
        monitor.setThreshold("A", 65.0);
        CapturingObserver obs = new CapturingObserver();
        monitor.register(obs);

        monitor.recordNoise("A", 50.0); // below
        monitor.recordNoise("A", 60.0); // below
        monitor.recordNoise("A", 75.0); // crosses upward → alert

        assertEquals(1, obs.received.size());
        assertEquals("NOISE_HIGH", obs.received.get(0).type());
        assertEquals(75.0, obs.received.get(0).value());
    }

    @Test
    void edge_triggered_no_repeat_alerts_while_above_threshold() {
        NoiseMonitor monitor = new NoiseMonitor();
        monitor.setThreshold("A", 65.0);
        CapturingObserver obs = new CapturingObserver();
        monitor.register(obs);

        monitor.recordNoise("A", 70.0);  // alert 1
        monitor.recordNoise("A", 75.0);  // still above — no new alert
        monitor.recordNoise("A", 90.0);  // still above — no new alert

        assertEquals(1, obs.received.size());
    }

    @Test
    void falling_below_then_rising_triggers_again() {
        NoiseMonitor monitor = new NoiseMonitor();
        monitor.setThreshold("A", 65.0);
        CapturingObserver obs = new CapturingObserver();
        monitor.register(obs);

        monitor.recordNoise("A", 70.0);  // alert 1
        monitor.recordNoise("A", 50.0);  // back below
        monitor.recordNoise("A", 80.0);  // alert 2

        assertEquals(2, obs.received.size());
    }

    @Test
    void multiple_observers_all_receive_alert() {
        NoiseMonitor monitor = new NoiseMonitor();
        monitor.setThreshold("A", 65.0);
        CapturingObserver o1 = new CapturingObserver();
        CapturingObserver o2 = new CapturingObserver();
        CapturingObserver o3 = new CapturingObserver();
        monitor.register(o1);
        monitor.register(o2);
        monitor.register(o3);

        monitor.recordNoise("A", 80.0);

        assertEquals(1, o1.received.size());
        assertEquals(1, o2.received.size());
        assertEquals(1, o3.received.size());
    }

    @Test
    void below_threshold_never_alerts() {
        NoiseMonitor monitor = new NoiseMonitor();
        monitor.setThreshold("A", 65.0);
        CapturingObserver obs = new CapturingObserver();
        monitor.register(obs);

        monitor.recordNoise("A", 40.0);
        monitor.recordNoise("A", 50.0);
        monitor.recordNoise("A", 64.99);

        assertEquals(0, obs.received.size());
    }
}
