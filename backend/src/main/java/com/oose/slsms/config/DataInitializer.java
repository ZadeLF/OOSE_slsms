package com.oose.slsms.config;

import com.oose.slsms.domain.Floor;
import com.oose.slsms.domain.Seat;
import com.oose.slsms.domain.Zone;
import com.oose.slsms.observer.NoiseMonitor;
import com.oose.slsms.observer.TemperatureMonitor;
import com.oose.slsms.repository.SeatRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * Seeds the in-memory store with one floor → one zone → 12 seats so the
 * demo has something to show on first request.
 */
@Component
public class DataInitializer {

    private final SeatRepository repository;
    private final NoiseMonitor noiseMonitor;
    private final TemperatureMonitor temperatureMonitor;

    public DataInitializer(SeatRepository repository, NoiseMonitor noiseMonitor,
                            TemperatureMonitor temperatureMonitor) {
        this.repository = repository;
        this.noiseMonitor = noiseMonitor;
        this.temperatureMonitor = temperatureMonitor;
    }

    @PostConstruct
    public void seed() {
        Floor floor = new Floor("1", "1F");
        Zone zoneA = new Zone("A", "閱覽 A 區", floor.getId(), 65.0);
        floor.addZone(zoneA);

        // 4 columns × 3 rows = 12 seats: A1..A4, B1..B4, C1..C4
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                String id = ((char) ('A' + row)) + String.valueOf(col + 1);
                Seat seat = new Seat(id, zoneA.getId(), row, col);
                zoneA.addSeat(seat);
                repository.save(seat);
            }
        }

        repository.saveZone(zoneA);
        repository.saveFloor(floor);
        noiseMonitor.setThreshold(zoneA.getId(), zoneA.getNoiseThresholdDb());
        temperatureMonitor.setBounds(zoneA.getId(), 18.0, 28.0);

        System.out.println("[DataInitializer] seeded floor=1F zone=A with 12 seats; "
                + "noise threshold = " + zoneA.getNoiseThresholdDb() + " dB; "
                + "temperature bounds = 18.0~28.0 °C");
    }
}
