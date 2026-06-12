package com.oose.slsms.domain;

import java.util.ArrayList;
import java.util.List;

public class Floor {
    private final String id;
    private final String name;
    private final List<Zone> zones = new ArrayList<>();

    public Floor(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addZone(Zone zone) { zones.add(zone); }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<Zone> getZones() { return zones; }
}
