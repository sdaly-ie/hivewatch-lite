package com.hivewatch.hivewatchlite.service;

import java.util.List;
import java.util.Optional;
import com.hivewatch.hivewatchlite.entity.Hive;

public interface HiveService {

    // CRUD
    Hive save(Hive hive);
    Optional<Hive> findById(Long id);
    Hive update(Long id, Hive updated);
    void deleteById(Long id);
    Iterable<Hive> findAll();

    // Query methods
    Optional<Hive> findByNameIgnoreCase(String name);
    List<Hive> findByLocationContainingIgnoreCase(String fragment);
    List<Hive> findByNameContainingIgnoreCase(String fragment);

    // Business methods
    Hive createHive(String name, String location);
    Hive renameHive(Long hiveId, String newName);
    Hive relocateHive(Long hiveId, String newLocation);
}