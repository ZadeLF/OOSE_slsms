package com.oose.slsms.repository;

import com.oose.slsms.domain.Floor;
import com.oose.slsms.domain.Seat;
import com.oose.slsms.domain.Zone;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Repository Pattern — in-memory implementation for the demo.
 * Swapping to Spring Data JPA only requires changing this file's
 * persistence calls; the service layer above is decoupled.
 */
@Repository
public class SeatRepository {

    private final ConcurrentMap<String, Seat> seats = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Zone> zones = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Floor> floors = new ConcurrentHashMap<>();

    public void save(Seat seat) { seats.put(seat.getId(), seat); }
    public void saveZone(Zone zone) { zones.put(zone.getId(), zone); }
    public void saveFloor(Floor floor) { floors.put(floor.getId(), floor); }

    public Optional<Seat> findById(String id) {
        return Optional.ofNullable(seats.get(id));
    }

    public List<Seat> findByZone(String zoneId) {
        List<Seat> out = new ArrayList<>();
        for (Seat s : seats.values()) {
            if (s.getZoneId().equals(zoneId)) out.add(s);
        }
        out.sort((a, b) -> a.getId().compareTo(b.getId()));
        return out;
    }

    public List<Seat> findByFloor(String floorId) {
        List<Seat> out = new ArrayList<>();
        Floor f = floors.get(floorId);
        if (f == null) return out;
        for (Zone z : f.getZones()) {
            out.addAll(findByZone(z.getId()));
        }
        return out;
    }

    public Optional<Zone> findZone(String id) { return Optional.ofNullable(zones.get(id)); }
    public Optional<Floor> findFloor(String id) { return Optional.ofNullable(floors.get(id)); }
    public List<Floor> findAllFloors() { return new ArrayList<>(floors.values()); }
    public List<Zone> findAllZones() { return new ArrayList<>(zones.values()); }
    public List<Seat> findAllSeats() { return new ArrayList<>(seats.values()); }
}
