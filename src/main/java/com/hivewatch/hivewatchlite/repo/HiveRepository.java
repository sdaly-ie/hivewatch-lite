package com.hivewatch.hivewatchlite.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hivewatch.hivewatchlite.entity.Hive;

public interface HiveRepository extends JpaRepository<Hive, Long> {

    Optional<Hive> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<Hive> findByLocationContainingIgnoreCase(String fragment);

    List<Hive> findByNameContainingIgnoreCase(String fragment);
}