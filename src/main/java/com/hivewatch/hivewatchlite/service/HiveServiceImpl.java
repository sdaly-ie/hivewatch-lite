package com.hivewatch.hivewatchlite.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hivewatch.hivewatchlite.entity.Hive;
import com.hivewatch.hivewatchlite.repo.HiveRepository;
import com.hivewatch.hivewatchlite.repo.TemperatureReadingRepository;

@Service
public class HiveServiceImpl implements HiveService {

    private final HiveRepository hiveRepository;
    private final TemperatureReadingRepository readingRepository;

    public HiveServiceImpl(HiveRepository hiveRepository, TemperatureReadingRepository readingRepository) {
        this.hiveRepository = hiveRepository;
        this.readingRepository = readingRepository;
    }

    // CRUD
    @Override
    @Transactional
    public Hive save(Hive hive) {
        validateHiveForSave(hive);
        enforceUniqueName(hive.getName(), null);
        return hiveRepository.save(hive);
    }

    @Override
    public Optional<Hive> findById(Long id) {
        return hiveRepository.findById(id);
    }

    @Override
    public Iterable<Hive> findAll() {
        return hiveRepository.findAll();
    }

    @Override
    @Transactional
    public Hive update(Long id, Hive updated) {
        validateHiveForSave(updated);

        Hive existing = hiveRepository.findById(id)
                .orElseThrow(() -> new HiveNotFoundException("Hive with id " + id + " not found"));

        // Business rule: you can update name/location, but name must remain unique
        String newName = normalize(updated.getName());
        String newLocation = normalize(updated.getLocation());

        enforceUniqueName(newName, existing.getId());

        existing.setName(newName);
        existing.setLocation(newLocation);

        return hiveRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!hiveRepository.existsById(id)) {
            throw new HiveNotFoundException("Hive with id " + id + " not found");
        }

        // don't allow deletion if readings exist
        long readingCount = readingRepository.countByHive_Id(id);
        if (readingCount > 0) {
            throw new IllegalStateException("Cannot delete hive " + id + " because it has " + readingCount + " readings");
        }

        hiveRepository.deleteById(id);
    }

    // query
    @Override
    public Optional<Hive> findByNameIgnoreCase(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        return hiveRepository.findByNameIgnoreCase(name.trim());
    }

    @Override
    public List<Hive> findByLocationContainingIgnoreCase(String fragment) {
        if (fragment == null || fragment.isBlank()) return List.of();
        return hiveRepository.findByLocationContainingIgnoreCase(fragment.trim());
    }

    @Override
    public List<Hive> findByNameContainingIgnoreCase(String fragment) {
        if (fragment == null || fragment.isBlank()) return List.of();
        return hiveRepository.findByNameContainingIgnoreCase(fragment.trim());
    }

    // business logic
    @Override
    @Transactional
    public Hive createHive(String name, String location) {
        String n = normalize(name);
        String l = normalize(location);

        validateHiveFields(n, l);
        enforceUniqueName(n, null);

        return hiveRepository.save(new Hive(n, l));
    }

    @Override
    @Transactional
    public Hive renameHive(Long hiveId, String newName) {
        Hive hive = hiveRepository.findById(hiveId)
                .orElseThrow(() -> new HiveNotFoundException("Hive with id " + hiveId + " not found"));

        String n = normalize(newName);
        validateHiveFields(n, hive.getLocation());
        enforceUniqueName(n, hive.getId());

        hive.setName(n);
        return hiveRepository.save(hive);
    }

    @Override
    @Transactional
    public Hive relocateHive(Long hiveId, String newLocation) {
        Hive hive = hiveRepository.findById(hiveId)
                .orElseThrow(() -> new HiveNotFoundException("Hive with id " + hiveId + " not found"));

        String l = normalize(newLocation);
        validateHiveFields(hive.getName(), l);

        hive.setLocation(l);
        return hiveRepository.save(hive);
    }

    // validation
    private void validateHiveForSave(Hive hive) {
        if (hive == null) throw new IllegalArgumentException("Hive must not be null");
        validateHiveFields(hive.getName(), hive.getLocation());
        hive.setName(normalize(hive.getName()));
        hive.setLocation(normalize(hive.getLocation()));
    }

    private void validateHiveFields(String name, String location) {
        requireNotBlank(name, "Hive name");
        requireNotBlank(location, "Hive location");

        if (name.length() < 2 || name.length() > 50) {
            throw new IllegalArgumentException("Hive name must be 2–50 characters");
        }
        if (location.length() < 2 || location.length() > 80) {
            throw new IllegalArgumentException("Hive location must be 2–80 characters");
        }
    }

    private void enforceUniqueName(String name, Long currentHiveId) {
        Optional<Hive> existing = hiveRepository.findByNameIgnoreCase(name);
        if (existing.isPresent()) {
            if (currentHiveId == null || !existing.get().getId().equals(currentHiveId)) {
                throw new IllegalArgumentException("Hive name must be unique. '" + name + "' already exists");
            }
        }
    }

    private void requireNotBlank(String s, String field) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }

    private String normalize(String s) {
        return (s == null) ? null : s.trim();
    }
}