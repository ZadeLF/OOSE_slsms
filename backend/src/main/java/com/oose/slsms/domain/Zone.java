package com.oose.slsms.domain;

import java.util.ArrayList;
import java.util.List;

public class Zone {
    private final String id;
    private final String name;
    private final String floorId;
    private final double noiseThresholdDb;
    private final List<Seat> seats = new ArrayList<>();

    public Zone(String id, String name, String floorId, double noiseThresholdDb) {
        this.id = id;
        this.name = name;
        this.floorId = floorId;
        this.noiseThresholdDb = noiseThresholdDb;
    }

    public void addSeat(Seat seat) { seats.add(seat); }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getFloorId() { return floorId; }
    public double getNoiseThresholdDb() { return noiseThresholdDb; }
    public List<Seat> getSeats() { return seats; }
}
