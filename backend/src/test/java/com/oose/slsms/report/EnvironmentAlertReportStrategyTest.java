package com.oose.slsms.report;

import com.oose.slsms.domain.Zone;
import com.oose.slsms.observer.AdminPushChannel;
import com.oose.slsms.observer.NoiseMonitor;
import com.oose.slsms.observer.TemperatureMonitor;
import com.oose.slsms.repository.SeatRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link EnvironmentAlertReportStrategy}.
 * Verifies that noise and temperature readings/alerts from both monitors
 * are correctly aggregated per zone.
 */
class EnvironmentAlertReportStrategyTest {

    @Test
    void aggregates_noise_and_temperature_alerts_per_zone() {
        SeatRepository repo = new SeatRepository();
        repo.saveZone(new Zone("A", "Zone A", "F1", 65.0));
        repo.saveZone(new Zone("B", "Zone B", "F1", 65.0));

        NoiseMonitor noiseMonitor = new NoiseMonitor();
        TemperatureMonitor temperatureMonitor = new TemperatureMonitor();
        AdminPushChannel adminPushChannel = new AdminPushChannel(noiseMonitor, temperatureMonitor);

        noiseMonitor.setThreshold("A", 65.0);
        temperatureMonitor.setBounds("A", 18.0, 28.0);

        // Zone A: trigger one noise alert and one temp-high alert
        noiseMonitor.recordNoise("A", 80.0);
        temperatureMonitor.recordTemperature("A", 30.0);

        // Zone B: stays within bounds, no alerts
        noiseMonitor.recordNoise("B", 40.0);
        temperatureMonitor.recordTemperature("B", 22.0);

        EnvironmentAlertReportStrategy strategy =
                new EnvironmentAlertReportStrategy(repo, noiseMonitor, temperatureMonitor, adminPushChannel);

        assertEquals("environment-alerts", strategy.type());

        EnvironmentAlertReport report = strategy.generate();

        assertEquals(2, report.totalRecentAlerts());
        assertEquals(2, report.byZone().size());

        ZoneEnvironment zoneA = report.byZone().stream()
                .filter(z -> z.zoneId().equals("A")).findFirst().orElseThrow();
        assertEquals("Zone A", zoneA.zoneName());
        assertEquals(80.0, zoneA.latestNoiseDb());
        assertEquals(65.0, zoneA.noiseThreshold());
        assertTrue(zoneA.noiseAlertActive());
        assertEquals(1L, zoneA.recentNoiseAlerts());
        assertEquals(30.0, zoneA.latestTemperature());
        assertEquals(18.0, zoneA.temperatureLowerBound());
        assertEquals(28.0, zoneA.temperatureUpperBound());
        assertTrue(zoneA.temperatureAlertActive());
        assertEquals(1L, zoneA.recentTemperatureAlerts());

        ZoneEnvironment zoneB = report.byZone().stream()
                .filter(z -> z.zoneId().equals("B")).findFirst().orElseThrow();
        assertFalse(zoneB.noiseAlertActive());
        assertEquals(0L, zoneB.recentNoiseAlerts());
        assertFalse(zoneB.temperatureAlertActive());
        assertEquals(0L, zoneB.recentTemperatureAlerts());
    }

    @Test
    void no_zones_yields_empty_report() {
        SeatRepository repo = new SeatRepository();
        NoiseMonitor noiseMonitor = new NoiseMonitor();
        TemperatureMonitor temperatureMonitor = new TemperatureMonitor();
        AdminPushChannel adminPushChannel = new AdminPushChannel(noiseMonitor, temperatureMonitor);

        EnvironmentAlertReportStrategy strategy =
                new EnvironmentAlertReportStrategy(repo, noiseMonitor, temperatureMonitor, adminPushChannel);

        EnvironmentAlertReport report = strategy.generate();

        assertEquals(0, report.totalRecentAlerts());
        assertTrue(report.byZone().isEmpty());
    }
}
