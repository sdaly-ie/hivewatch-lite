package com.hivewatch.hivewatchlite.dto;

import java.time.LocalDateTime;

import com.hivewatch.hivewatchlite.entity.Hive;
import com.hivewatch.hivewatchlite.entity.TemperatureReading;

//TemperatureReadingDTO - Reading data returned, including hiveId/hiveName (to avoid nested Hive objects)
public class TemperatureReadingDTO {

    private Long id;
    private double temperature;
    private LocalDateTime recordedAt;

    private Long hiveId;
    private String hiveName;

    public TemperatureReadingDTO() {}

    public TemperatureReadingDTO(TemperatureReading reading) {
        this.id = reading.getId();
        this.temperature = reading.getTemperature();
        this.recordedAt = reading.getRecordedAt();

        Hive hive = reading.getHive();
        if (hive != null) {
            this.hiveId = hive.getId();
            this.hiveName = hive.getName();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }

    public Long getHiveId() { return hiveId; }
    public void setHiveId(Long hiveId) { this.hiveId = hiveId; }

    public String getHiveName() { return hiveName; }
    public void setHiveName(String hiveName) { this.hiveName = hiveName; }
}