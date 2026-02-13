package com.hivewatch.hivewatchlite.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hivewatch.hivewatchlite.entity.Hive;

public interface HiveRepository extends JpaRepository<Hive, Long> { }