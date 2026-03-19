package com.hivewatch.hivewatchlite.dto;

// WriteHiveDTO - Hive data the client sends to create or update a Hive, without an id.
public class WriteHiveDTO {

    private String name;
    private String location;

    public WriteHiveDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}