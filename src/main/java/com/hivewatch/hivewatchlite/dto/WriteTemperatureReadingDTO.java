package com.hivewatch.hivewatchlite.dto;

import java.time.LocalDateTime;

//WriteTemperatureReadingDTO - Reads data the client sends, with recordedAt ISO date and time format with hiveId to link it to a Hive
public class WriteTemperatureReadingDTO {

    private Double temperature;
    private LocalDateTime recordedAt;
    private Long hiveId;

    public WriteTemperatureReadingDTO() {}

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }

    public Long getHiveId() { return hiveId; }
    public void setHiveId(Long hiveId) { this.hiveId = hiveId; }
}