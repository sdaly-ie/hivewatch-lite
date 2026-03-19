package com.hivewatch.hivewatchlite.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.hivewatch.hivewatchlite.entity.TemperatureReading;

public interface TemperatureReadingService {

    // CRUD
    TemperatureReading save(TemperatureReading reading);
    Optional<TemperatureReading> findById(Long id);
    TemperatureReading update(Long id, TemperatureReading updated);
    void deleteById(Long id);
    Iterable<TemperatureReading> findAll();

    // query
    List<TemperatureReading> findByHiveIdOrderByRecordedAtDesc(Long hiveId);
    Optional<TemperatureReading> findLatestForHive(Long hiveId);
    List<TemperatureReading> findByHiveIdBetween(Long hiveId, LocalDateTime start, LocalDateTime end);
    long countByHiveId(Long hiveId);

    // business logic
    TemperatureReading recordReading(Long hiveId, double temperature, LocalDateTime recordedAt);
    TemperatureReading recordNow(Long hiveId, double temperature);
    double averageTempLastMinutes(Long hiveId, int minutes);
    TemperatureReading assignToHive(Long readingId, Long hiveId);
    int applyOffsetToHive(Long hiveId, double delta);
}