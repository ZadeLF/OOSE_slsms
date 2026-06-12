package com.oose.slsms;

import com.oose.slsms.observer.AlertEvent;
import com.oose.slsms.observer.AlertObserver;
import com.oose.slsms.observer.NoiseMonitor;
import com.oose.slsms.observer.TemperatureMonitor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TemperatureMonitor (Subject) — extends the existing
 * Observer architecture (same AlertObserver interface) to a second
 * environmental indicator with both an upper and a lower bound.
 */
class TemperatureObserverTest {

    /** A capturing test-double observer (same shape as NoiseObserverTest). */
    static class CapturingObserver implements AlertObserver {
        final List<AlertEvent> received = new ArrayList<>();
        @Override public String channelName() { return "TEST"; }
        @Override public void onAlert(AlertEvent event) { received.add(event); }
    }

    @Test
    void crossing_upper_bound_triggers_temp_high_alert() {
        TemperatureMonitor monitor = new TemperatureMonitor();
        monitor.setBounds("A", 18.0, 28.0);
        CapturingObserver obs = new CapturingObserver();
        monitor.register(obs);

        monitor.recordTemperature("A", 22.0); // within range
        monitor.recordTemperature("A", 30.0); // crosses upper bound → alert

        assertEquals(1, obs.received.size());
        assertEquals("TEMP_HIGH", obs.received.get(0).type());
        assertEquals(30.0, obs.received.get(0).value());
    }

    @Test
    void crossing_lower_bound_triggers_temp_low_alert() {
        TemperatureMonitor monitor = new TemperatureMonitor();
        monitor.setBounds("A", 18.0, 28.0);
        CapturingObserver obs = new CapturingObserver();
        monitor.register(obs);

        monitor.recordTemperature("A", 22.0); // within range
        monitor.recordTemperature("A", 10.0); // crosses lower bound → alert

        assertEquals(1, obs.received.size());
        assertEquals("TEMP_LOW", obs.received.get(0).type());
        assertEquals(10.0, obs.received.get(0).value());
    }

    @Test
    void edge_triggered_no_repeat_alerts_while_out_of_range() {
        TemperatureMonitor monitor = new TemperatureMonitor();
        monitor.setBounds("A", 18.0, 28.0);
        CapturingObserver obs = new CapturingObserver();
        monitor.register(obs);

        monitor.recordTemperature("A", 30.0); // alert 1 (TEMP_HIGH)
        monitor.recordTemperature("A", 32.0); // still above — no new alert
        monitor.recordTemperature("A", 35.0); // still above — no new alert

        assertEquals(1, obs.received.size());
    }

    @Test
    void returning_within_range_then_crossing_again_triggers_again() {
        TemperatureMonitor monitor = new TemperatureMonitor();
        monitor.setBounds("A", 18.0, 28.0);
        CapturingObserver obs = new CapturingObserver();
        monitor.register(obs);

        monitor.recordTemperature("A", 30.0); // alert 1 (TEMP_HIGH)
        monitor.recordTemperature("A", 22.0); // back within range — alert cleared
        monitor.recordTemperature("A", 31.0); // alert 2 (TEMP_HIGH)

        assertEquals(2, obs.received.size());
        assertEquals("TEMP_HIGH", obs.received.get(1).type());
    }

    @Test
    void within_bounds_never_alerts() {
        TemperatureMonitor monitor = new TemperatureMonitor();
        monitor.setBounds("A", 18.0, 28.0);
        CapturingObserver obs = new CapturingObserver();
        monitor.register(obs);

        monitor.recordTemperature("A", 18.0);
        monitor.recordTemperature("A", 23.5);
        monitor.recordTemperature("A", 28.0);

        assertEquals(0, obs.received.size());
        assertFalse(monitor.isAlertActive("A"));
    }

    @Test
    void multiple_observers_all_receive_alert() {
        TemperatureMonitor monitor = new TemperatureMonitor();
        monitor.setBounds("A", 18.0, 28.0);
        CapturingObserver o1 = new CapturingObserver();
        CapturingObserver o2 = new CapturingObserver();
        CapturingObserver o3 = new CapturingObserver();
        monitor.register(o1);
        monitor.register(o2);
        monitor.register(o3);

        monitor.recordTemperature("A", 32.0);

        assertEquals(1, o1.received.size());
        assertEquals(1, o2.received.size());
        assertEquals(1, o3.received.size());
    }

    @Test
    void noise_and_temperature_monitors_operate_independently() {
        NoiseMonitor noise = new NoiseMonitor();
        noise.setThreshold("A", 65.0);
        TemperatureMonitor temp = new TemperatureMonitor();
        temp.setBounds("A", 18.0, 28.0);

        CapturingObserver noiseObs = new CapturingObserver();
        CapturingObserver tempObs = new CapturingObserver();
        noise.register(noiseObs);
        temp.register(tempObs);

        // Trigger only the noise alert.
        noise.recordNoise("A", 80.0);
        assertEquals(1, noiseObs.received.size());
        assertEquals("NOISE_HIGH", noiseObs.received.get(0).type());
        assertEquals(0, tempObs.received.size());

        // Trigger only the temperature alert.
        temp.recordTemperature("A", 5.0);
        assertEquals(1, tempObs.received.size());
        assertEquals("TEMP_LOW", tempObs.received.get(0).type());
        assertEquals(1, noiseObs.received.size()); // unchanged
    }
}
