package com.hivewatch.hivewatchlite.dto;

import com.hivewatch.hivewatchlite.entity.Hive;

//HiveDTO - Simplified Hive data returned to the client instead of the entity.
public class HiveDTO {

    private Long id;
    private String name;
    private String location;

    public HiveDTO() {}

    public HiveDTO(Hive hive) {
        this.id = hive.getId();
        this.name = hive.getName();
        this.location = hive.getLocation();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}