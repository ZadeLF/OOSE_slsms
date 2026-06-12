package com.oose.slsms.service;

import com.oose.slsms.domain.Seat;
import com.oose.slsms.exception.SeatNotFoundException;
import com.oose.slsms.repository.SeatRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer — coordinates state transitions on Seat and writes
 * audit/usage records. Defers all transition decisions to the SeatState
 * objects on the seat itself (State Pattern).
 */
@Service
public class SeatService {

    private final SeatRepository repository;

    public SeatService(SeatRepository repository) {
        this.repository = repository;
    }

    public List<Seat> listByFloor(String floorId) {
        return repository.findByFloor(floorId);
    }

    public List<Seat> listByZone(String zoneId) {
        return repository.findByZone(zoneId);
    }

    public Seat get(String seatId) {
        return repository.findById(seatId).orElseThrow(() -> new SeatNotFoundException(seatId));
    }

    public Seat reserve(String seatId, String userId) {
        Seat seat = get(seatId);
        seat.reserve(userId);
        repository.save(seat);
        return seat;
    }

    public Seat checkIn(String seatId, String userId) {
        Seat seat = get(seatId);
        seat.checkIn(userId);
        repository.save(seat);
        return seat;
    }

    public Seat leaveTemporarily(String seatId, String userId) {
        Seat seat = get(seatId);
        seat.leaveTemporarily(userId);
        repository.save(seat);
        return seat;
    }

    public Seat comeBack(String seatId, String userId) {
        Seat seat = get(seatId);
        seat.comeBack(userId);
        repository.save(seat);
        return seat;
    }

    public Seat release(String seatId, String userId) {
        Seat seat = get(seatId);
        seat.release(userId);
        repository.save(seat);
        return seat;
    }
}
