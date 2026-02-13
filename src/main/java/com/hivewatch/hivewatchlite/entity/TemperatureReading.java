package com.hivewatch.hivewatchlite.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class TemperatureReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double temperature;
    private LocalDateTime recordedAt;

    @ManyToOne
    @JoinColumn(name = "hive_id")
    private Hive hive;

    public TemperatureReading() {}

    public TemperatureReading(double temperature, LocalDateTime recordedAt, Hive hive) {
        this.temperature = temperature;
        this.recordedAt = recordedAt;
        this.hive = hive;
    }

    // Getters/Setters
    public Long getId() { return id; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }

    public Hive getHive() { return hive; }
    public void setHive(Hive hive) { this.hive = hive; }
}
