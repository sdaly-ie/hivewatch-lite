package com.hivewatch.hivewatchlite.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hivewatch.hivewatchlite.entity.Hive;
import com.hivewatch.hivewatchlite.entity.TemperatureReading;
import com.hivewatch.hivewatchlite.repo.HiveRepository;
import com.hivewatch.hivewatchlite.repo.TemperatureReadingRepository;

@Service
public class TemperatureReadingServiceImpl implements TemperatureReadingService {

    private final TemperatureReadingRepository readingRepository;
    private final HiveRepository hiveRepository;

    public TemperatureReadingServiceImpl(TemperatureReadingRepository readingRepository, HiveRepository hiveRepository) {
        this.readingRepository = readingRepository;
        this.hiveRepository = hiveRepository;
    }

    // CRUD

    @Override
    @Transactional
    public TemperatureReading save(TemperatureReading reading) {
        validateReadingForSave(reading);

        // hive must exist
        Hive hive = reading.getHive();
        if (hive == null || hive.getId() == null) {
            throw new IllegalArgumentException("TemperatureReading must reference a persisted Hive (non-null hive.id)");
        }
        if (!hiveRepository.existsById(hive.getId())) {
            throw new IllegalArgumentException("Hive with id " + hive.getId() + " does not exist");
        }

        // prevent duplicate timestamp per hive
        if (readingRepository.existsByHive_IdAndRecordedAt(hive.getId(), reading.getRecordedAt())) {
            throw new IllegalArgumentException("Duplicate reading for hive " + hive.getId() + " at " + reading.getRecordedAt());
        }

        return readingRepository.save(reading);
    }

    @Override
    public Optional<TemperatureReading> findById(Long id) {
        return readingRepository.findById(id);
    }

    @Override
    public Iterable<TemperatureReading> findAll() {
        return readingRepository.findAll();
    }

    @Override
    @Transactional
    public TemperatureReading update(Long id, TemperatureReading updated) {
        validateReadingForSave(updated);

        TemperatureReading existing = readingRepository.findById(id)
                .orElseThrow(() -> new TemperatureReadingNotFoundException("Reading with id " + id + " not found"));

        // don't allow changing hive on update
        if (updated.getHive() == null || updated.getHive().getId() == null) {
            throw new IllegalArgumentException("Reading must reference its hive (non-null hive.id)");
        }
        if (!updated.getHive().getId().equals(existing.getHive().getId())) {
            throw new IllegalArgumentException("Cannot move a reading to another hive (immutable relationship)");
        }

        // prevent duplicate timestamp on update
        Long hiveId = existing.getHive().getId();
        LocalDateTime newRecordedAt = updated.getRecordedAt();

        if (!existing.getRecordedAt().equals(newRecordedAt)
                && readingRepository.existsByHive_IdAndRecordedAt(hiveId, newRecordedAt)) {
            throw new IllegalArgumentException("Duplicate reading for hive " + hiveId + " at " + newRecordedAt);
        }

        existing.setTemperature(updated.getTemperature());
        existing.setRecordedAt(newRecordedAt);

        return readingRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!readingRepository.existsById(id)) {
            throw new TemperatureReadingNotFoundException("Reading with id " + id + " not found");
        }
        readingRepository.deleteById(id);
    }

    // query
    @Override
    public List<TemperatureReading> findByHiveIdOrderByRecordedAtDesc(Long hiveId) {
        requireHiveExists(hiveId);
        return readingRepository.findByHive_IdOrderByRecordedAtDesc(hiveId);
    }

    @Override
    public Optional<TemperatureReading> findLatestForHive(Long hiveId) {
        requireHiveExists(hiveId);
        return readingRepository.findTopByHive_IdOrderByRecordedAtDesc(hiveId);
    }

    @Override
    public List<TemperatureReading> findByHiveIdBetween(Long hiveId, LocalDateTime start, LocalDateTime end) {
        requireHiveExists(hiveId);
        if (start == null || end == null) throw new IllegalArgumentException("Start/end must not be null");
        if (start.isAfter(end)) throw new IllegalArgumentException("Start must be <= end");
        return readingRepository.findByHive_IdAndRecordedAtBetween(hiveId, start, end);
    }

    @Override
    public long countByHiveId(Long hiveId) {
        requireHiveExists(hiveId);
        return readingRepository.countByHive_Id(hiveId);
    }

    // business logic
    @Override
    @Transactional
    public TemperatureReading recordReading(Long hiveId, double temperature, LocalDateTime recordedAt) {
        requireHiveExists(hiveId);
        if (recordedAt == null) throw new IllegalArgumentException("recordedAt is required");

        Hive hive = hiveRepository.findById(hiveId).orElseThrow();

        TemperatureReading reading = new TemperatureReading(temperature, recordedAt, hive);

        return save(reading);
    }

    @Override
    @Transactional
    public TemperatureReading recordNow(Long hiveId, double temperature) {
        return recordReading(hiveId, temperature, LocalDateTime.now());
    }

    @Override
    public double averageTempLastMinutes(Long hiveId, int minutes) {
        requireHiveExists(hiveId);
        if (minutes <= 0) throw new IllegalArgumentException("minutes must be > 0");
        if (minutes > 24 * 60) throw new IllegalArgumentException("minutes too large (max 1440)");

        LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);
        Double avg = readingRepository.findAverageTempSince(hiveId, since);
        return (avg == null) ? Double.NaN : avg;
    }
    
    @Override
    @Transactional
    public TemperatureReading assignToHive(Long readingId, Long hiveId) {
        requireHiveExists(hiveId);

        TemperatureReading existing = readingRepository.findById(readingId)
                .orElseThrow(() -> new TemperatureReadingNotFoundException("Reading with id " + readingId + " not found"));

        Long currentHiveId = (existing.getHive() == null) ? null : existing.getHive().getId();
        if (currentHiveId != null && currentHiveId.equals(hiveId)) {
            return existing; // already assigned
        }

        if (readingRepository.existsByHive_IdAndRecordedAt(hiveId, existing.getRecordedAt())) {
            throw new IllegalArgumentException(
                    "Cannot assign reading " + readingId + " to hive " + hiveId +
                    " because a reading already exists at " + existing.getRecordedAt());
        }

        Hive target = hiveRepository.findById(hiveId).orElseThrow();
        existing.setHive(target);
        return readingRepository.save(existing);
    }

    @Override
    @Transactional
    public int applyOffsetToHive(Long hiveId, double delta) {
        requireHiveExists(hiveId);

        if (delta < -20.0 || delta > 20.0) {
            throw new IllegalArgumentException("delta must be between -20.0 and +20.0");
        }

        return readingRepository.applyOffsetToHive(hiveId, delta);
    }

    // validation
    private void validateReadingForSave(TemperatureReading reading) {
        if (reading == null) throw new IllegalArgumentException("Reading must not be null");

        // domain bounds for temp sensors
        double t = reading.getTemperature();
        if (t < -9.0 || t > 46.5) {
            throw new IllegalArgumentException("Temperature must be between -9.0 and +46.5 C");
        }

        LocalDateTime at = reading.getRecordedAt();
        if (at == null) throw new IllegalArgumentException("recordedAt is required");

        // prevents timestamp in the future 
        if (at.isAfter(LocalDateTime.now().plusSeconds(5))) {
            throw new IllegalArgumentException("recordedAt cannot be in the future");
        }
    }

    private void requireHiveExists(Long hiveId) {
        if (hiveId == null) throw new IllegalArgumentException("hiveId is required");
        if (!hiveRepository.existsById(hiveId)) {
            throw new HiveNotFoundException("Hive with id " + hiveId + " not found");
        }
    }
}