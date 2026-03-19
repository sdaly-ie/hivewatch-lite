package com.hivewatch.hivewatchlite.repo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.hivewatch.hivewatchlite.entity.TemperatureReading;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface TemperatureReadingRepository extends JpaRepository<TemperatureReading, Long> {

    List<TemperatureReading> findByHive_IdOrderByRecordedAtDesc(Long hiveId);

    Optional<TemperatureReading> findTopByHive_IdOrderByRecordedAtDesc(Long hiveId);

    List<TemperatureReading> findByHive_IdAndRecordedAtBetween(Long hiveId, LocalDateTime start, LocalDateTime end);

    long countByHive_Id(Long hiveId);

    boolean existsByHive_IdAndRecordedAt(Long hiveId, LocalDateTime recordedAt);

    @Query("select avg(r.temperature) from TemperatureReading r where r.hive.id = :hiveId and r.recordedAt >= :since")
    Double findAverageTempSince(@Param("hiveId") Long hiveId, @Param("since") LocalDateTime since);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update TemperatureReading r set r.temperature = r.temperature + :delta where r.hive.id = :hiveId")
    int applyOffsetToHive(@Param("hiveId") Long hiveId, @Param("delta") double delta);
}