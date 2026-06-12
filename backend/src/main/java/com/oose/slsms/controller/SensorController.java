package com.oose.slsms.controller;

import com.oose.slsms.dto.NoiseRequest;
import com.oose.slsms.observer.NoiseMonitor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {

    private final NoiseMonitor noiseMonitor;

    public SensorController(NoiseMonitor noiseMonitor) {
        this.noiseMonitor = noiseMonitor;
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
}
