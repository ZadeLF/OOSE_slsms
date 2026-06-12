package com.oose.slsms.report;

import com.oose.slsms.domain.Zone;
import com.oose.slsms.observer.AdminPushChannel;
import com.oose.slsms.observer.AlertEvent;
import com.oose.slsms.observer.NoiseMonitor;
import com.oose.slsms.observer.TemperatureMonitor;
import com.oose.slsms.repository.SeatRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Strategy Pattern — concrete report: environment alert hotspots (noise + temperature).
 *
 * For each zone, combines the latest readings/thresholds from
 * {@link NoiseMonitor} and {@link TemperatureMonitor} with how many of the
 * recent alerts buffered in {@link AdminPushChannel} originated from that
 * zone — i.e. which zones are the current "hotspots" for environmental
 * complaints.
 */
@Component
public class EnvironmentAlertReportStrategy implements ReportStrategy {

    private final SeatRepository repository;
    private final NoiseMonitor noiseMonitor;
    private final TemperatureMonitor temperatureMonitor;
    private final AdminPushChannel adminPushChannel;

    public EnvironmentAlertReportStrategy(SeatRepository repository,
                                           NoiseMonitor noiseMonitor,
                                           TemperatureMonitor temperatureMonitor,
                                           AdminPushChannel adminPushChannel) {
        this.repository = repository;
        this.noiseMonitor = noiseMonitor;
        this.temperatureMonitor = temperatureMonitor;
        this.adminPushChannel = adminPushChannel;
    }

    @Override
    public String type() {
        return "environment-alerts";
    }

    @Override
    public String title() {
        return "環境警示熱點報表（噪音/溫度）";
    }

    @Override
    public EnvironmentAlertReport generate() {
        List<AlertEvent> recent = adminPushChannel.recent();

        List<ZoneEnvironment> byZone = repository.findAllZones().stream()
                .map(zone -> toZoneEnvironment(zone, recent))
                .sorted(Comparator.comparing(ZoneEnvironment::zoneId))
                .toList();

        return new EnvironmentAlertReport(type(), title(), recent.size(), byZone);
    }

    private ZoneEnvironment toZoneEnvironment(Zone zone, List<AlertEvent> recent) {
        String zoneId = zone.getId();

        long noiseAlerts = recent.stream()
                .filter(a -> a.zoneId().equals(zoneId) && "NOISE_HIGH".equals(a.type()))
                .count();
        long tempAlerts = recent.stream()
                .filter(a -> a.zoneId().equals(zoneId) && a.type().startsWith("TEMP_"))
                .count();

        return new ZoneEnvironment(
                zoneId,
                zone.getName(),
                noiseMonitor.getLatest(zoneId),
                noiseMonitor.getThreshold(zoneId),
                noiseMonitor.isAlertActive(zoneId),
                noiseAlerts,
                temperatureMonitor.getLatest(zoneId),
                temperatureMonitor.getLowerBound(zoneId),
                temperatureMonitor.getUpperBound(zoneId),
                temperatureMonitor.isAlertActive(zoneId),
                tempAlerts);
    }
}
