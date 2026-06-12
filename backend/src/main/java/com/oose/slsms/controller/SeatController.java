package com.oose.slsms.controller;

import com.oose.slsms.dto.ActionRequest;
import com.oose.slsms.dto.SeatDto;
import com.oose.slsms.service.SeatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping("/floors/{floorId}/seats")
    public List<SeatDto> listByFloor(@PathVariable String floorId) {
        return seatService.listByFloor(floorId).stream().map(SeatDto::from).toList();
    }

    @GetMapping("/zones/{zoneId}/seats")
    public List<SeatDto> listByZone(@PathVariable String zoneId) {
        return seatService.listByZone(zoneId).stream().map(SeatDto::from).toList();
    }

    @GetMapping("/seats/{seatId}")
    public SeatDto get(@PathVariable String seatId) {
        return SeatDto.from(seatService.get(seatId));
    }

    @PostMapping("/seats/{seatId}/reserve")
    public SeatDto reserve(@PathVariable String seatId, @Valid @RequestBody ActionRequest req) {
        return SeatDto.from(seatService.reserve(seatId, req.userId()));
    }

    @PostMapping("/seats/{seatId}/checkin")
    public SeatDto checkIn(@PathVariable String seatId, @Valid @RequestBody ActionRequest req) {
        return SeatDto.from(seatService.checkIn(seatId, req.userId()));
    }

    @PostMapping("/seats/{seatId}/leave-temp")
    public SeatDto leaveTemp(@PathVariable String seatId, @Valid @RequestBody ActionRequest req) {
        return SeatDto.from(seatService.leaveTemporarily(seatId, req.userId()));
    }

    @PostMapping("/seats/{seatId}/come-back")
    public SeatDto comeBack(@PathVariable String seatId, @Valid @RequestBody ActionRequest req) {
        return SeatDto.from(seatService.comeBack(seatId, req.userId()));
    }

    @PostMapping("/seats/{seatId}/release")
    public SeatDto release(@PathVariable String seatId, @Valid @RequestBody ActionRequest req) {
        return SeatDto.from(seatService.release(seatId, req.userId()));
    }
}
