package com.hivewatch.hivewatchlite.repo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.hivewatch.hivewatchlite.entity.Hive;

@DataJpaTest
@ActiveProfiles("test")
class HiveRepositoryTest {

    @Autowired
    private HiveRepository hiveRepository;

    @Test
    void findByNameIgnoreCase_returnsMatchingHive() {
        hiveRepository.save(new Hive("Hive Alpha", "North Field"));

        Optional<Hive> result = hiveRepository.findByNameIgnoreCase("hive alpha");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Hive Alpha");
    }

    @Test
    void existsByNameIgnoreCase_returnsTrueWhenHiveExists() {
        hiveRepository.save(new Hive("Hive Bravo", "South Field"));

        boolean exists = hiveRepository.existsByNameIgnoreCase("hive bravo");

        assertThat(exists).isTrue();
    }

    @Test
    void findByLocationContainingIgnoreCase_returnsMatchingHives() {
        hiveRepository.save(new Hive("Hive One", "North Garden"));
        hiveRepository.save(new Hive("Hive Two", "South Orchard"));
        hiveRepository.save(new Hive("Hive Three", "North Meadow"));

        List<Hive> results = hiveRepository.findByLocationContainingIgnoreCase("north");

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Hive::getName)
                .containsExactlyInAnyOrder("Hive One", "Hive Three");
    }
}