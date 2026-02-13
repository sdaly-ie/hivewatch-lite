package com.hivewatch.hivewatchlite.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hivewatch.hivewatchlite.entity.TemperatureReading;

public interface TemperatureReadingRepository extends JpaRepository<TemperatureReading, Long> { }