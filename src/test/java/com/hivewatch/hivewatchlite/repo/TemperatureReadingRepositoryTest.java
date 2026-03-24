package com.hivewatch.hivewatchlite.repo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.hivewatch.hivewatchlite.entity.Hive;
import com.hivewatch.hivewatchlite.entity.TemperatureReading;

@DataJpaTest
@ActiveProfiles("test")
class TemperatureReadingRepositoryTest {

    @Autowired
    private HiveRepository hiveRepository;

    @Autowired
    private TemperatureReadingRepository temperatureReadingRepository;

    @Test
    void findByHiveIdOrderByRecordedAtDesc_returnsNewestFirst() {
        LocalDateTime base = LocalDateTime.of(2026, 3, 24, 12, 0);

        Hive hive = hiveRepository.save(new Hive("Hive Alpha", "North Field"));

        TemperatureReading older = new TemperatureReading(30.0, base.minusHours(2), hive);
        TemperatureReading newer = new TemperatureReading(35.0, base.minusHours(1), hive);

        temperatureReadingRepository.save(older);
        temperatureReadingRepository.save(newer);

        List<TemperatureReading> results =
                temperatureReadingRepository.findByHive_IdOrderByRecordedAtDesc(hive.getId());

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getTemperature()).isEqualTo(35.0);
        assertThat(results.get(1).getTemperature()).isEqualTo(30.0);
        assertThat(results.get(0).getRecordedAt()).isAfter(results.get(1).getRecordedAt());
    }

    @Test
    void findAverageTempSince_returnsAverageForRecentReadings() {
        LocalDateTime base = LocalDateTime.of(2026, 3, 24, 12, 0);

        Hive hive = hiveRepository.save(new Hive("Hive Beta", "South Field"));

        temperatureReadingRepository.save(
                new TemperatureReading(28.0, base.minusMinutes(30), hive));
        temperatureReadingRepository.save(
                new TemperatureReading(34.0, base.minusMinutes(5), hive));
        temperatureReadingRepository.save(
                new TemperatureReading(36.0, base.minusMinutes(2), hive));

        Double result = temperatureReadingRepository.findAverageTempSince(
                hive.getId(), base.minusMinutes(10));

        assertThat(result).isEqualTo(35.0);
    }

    @Test
    void applyOffsetToHive_updatesOnlyThatHivesReadings() {
        LocalDateTime base = LocalDateTime.of(2026, 3, 24, 12, 0);

        Hive hive1 = hiveRepository.save(new Hive("Hive Gamma", "East Field"));
        Hive hive2 = hiveRepository.save(new Hive("Hive Delta", "West Field"));

        temperatureReadingRepository.save(
                new TemperatureReading(20.0, base.minusMinutes(20), hive1));
        temperatureReadingRepository.save(
                new TemperatureReading(22.0, base.minusMinutes(10), hive1));
        temperatureReadingRepository.save(
                new TemperatureReading(15.0, base.minusMinutes(5), hive2));

        int updatedCount = temperatureReadingRepository.applyOffsetToHive(hive1.getId(), 1.5);

        List<TemperatureReading> hive1Results =
                temperatureReadingRepository.findByHive_IdOrderByRecordedAtDesc(hive1.getId());
        List<TemperatureReading> hive2Results =
                temperatureReadingRepository.findByHive_IdOrderByRecordedAtDesc(hive2.getId());

        assertThat(updatedCount).isEqualTo(2);
        assertThat(hive1Results).extracting(TemperatureReading::getTemperature)
                .containsExactlyInAnyOrder(21.5, 23.5);
        assertThat(hive2Results).extracting(TemperatureReading::getTemperature)
                .containsExactly(15.0);
    }
}