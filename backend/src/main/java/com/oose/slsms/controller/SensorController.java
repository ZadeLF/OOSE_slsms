package com.oose.slsms.controller;

import com.oose.slsms.dto.NoiseRequest;
import com.oose.slsms.dto.TemperatureRequest;
import com.oose.slsms.observer.NoiseMonitor;
import com.oose.slsms.observer.TemperatureMonitor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {

    private final NoiseMonitor noiseMonitor;
    private final TemperatureMonitor temperatureMonitor;

    public SensorController(NoiseMonitor noiseMonitor, TemperatureMonitor temperatureMonitor) {
        this.noiseMonitor = noiseMonitor;
        this.temperatureMonitor = temperatureMonitor;
    }

    /**
     * Frontend (or an MQTT bridge) pushes a noise reading here.
     * Edge-triggered alerts are dispatched inside NoiseMonitor.
     */
    @PostMapping("/noise")
    public Map<String, Object> recordNoise(@Valid @RequestBody NoiseRequest req) {
        noiseMonitor.recordNoise(req.zoneId(), req.dB());
        return Map.of(
                "zoneId", req.zoneId(),
                "dB", req.dB(),
                "threshold", noiseMonitor.getThreshold(req.zoneId()),
                "alertActive", noiseMonitor.isAlertActive(req.zoneId())
        );
    }

    @GetMapping("/noise/{zoneId}")
    public Map<String, Object> get(@PathVariable String zoneId) {
        return Map.of(
                "zoneId", zoneId,
                "dB", noiseMonitor.getLatest(zoneId),
                "threshold", noiseMonitor.getThreshold(zoneId),
                "alertActive", noiseMonitor.isAlertActive(zoneId)
        );
    }

    /**
     * Frontend temperature simulator pushes a reading here.
     * Reuses the same Observer architecture as noise: edge-triggered
     * alerts are dispatched inside TemperatureMonitor.
     */
    @PostMapping("/temperature")
    public Map<String, Object> recordTemperature(@Valid @RequestBody TemperatureRequest req) {
        temperatureMonitor.recordTemperature(req.zoneId(), req.celsius());
        return Map.of(
                "zoneId", req.zoneId(),
                "celsius", req.celsius(),
                "lowerBound", temperatureMonitor.getLowerBound(req.zoneId()),
                "upperBound", temperatureMonitor.getUpperBound(req.zoneId()),
                "alertActive", temperatureMonitor.isAlertActive(req.zoneId())
        );
    }

    @GetMapping("/temperature/{zoneId}")
    public Map<String, Object> getTemperature(@PathVariable String zoneId) {
        return Map.of(
                "zoneId", zoneId,
                "celsius", temperatureMonitor.getLatest(zoneId),
                "lowerBound", temperatureMonitor.getLowerBound(zoneId),
                "upperBound", temperatureMonitor.getUpperBound(zoneId),
                "alertActive", temperatureMonitor.isAlertActive(zoneId)
        );
    }
}
