package com.hivewatch.hivewatchlite.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Hive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;

    @OneToMany(mappedBy = "hive", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TemperatureReading> temperatureReadings = new ArrayList<>();

    public Hive() {}

    public Hive(String name, String location) {
        this.name = name;
        this.location = location;
    }

    // helper (optional but nice)
    public void addTemperatureReading(TemperatureReading reading) {
        temperatureReadings.add(reading);
        reading.setHive(this);
    }

    // Getters/Setters
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public List<TemperatureReading> getTemperatureReadings() { return temperatureReadings; }
}
