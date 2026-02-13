package com.hivewatch.hivewatchlite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.hivewatch.hivewatchlite.entity.Hive;
import com.hivewatch.hivewatchlite.entity.TemperatureReading;
import com.hivewatch.hivewatchlite.repo.HiveRepository;
import com.hivewatch.hivewatchlite.repo.TemperatureReadingRepository;

import java.time.LocalDateTime;

@SpringBootApplication
public class HivewatchliteApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(HivewatchliteApplication.class);

    private final HiveRepository hiveRepository;
    private final TemperatureReadingRepository temperatureReadingRepository;

    public HivewatchliteApplication(HiveRepository hiveRepository,
                                    TemperatureReadingRepository temperatureReadingRepository) {
        this.hiveRepository = hiveRepository;
        this.temperatureReadingRepository = temperatureReadingRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(HivewatchliteApplication.class, args);
        logger.info("HiveWatch Lite started");
    }

    @Override
    public void run(String... args) {
        // Create 2 hives (Entity #1)
        Hive hive1 = new Hive("Hive A", "Back Garden");
        Hive hive2 = new Hive("Hive B", "Front Garden");

        hiveRepository.save(hive1);
        hiveRepository.save(hive2);

        // Create a few temperature readings (Entity #2) related to the hives
        TemperatureReading r1 = new TemperatureReading(34.2, LocalDateTime.now().minusMinutes(10), hive1);
        TemperatureReading r2 = new TemperatureReading(33.8, LocalDateTime.now().minusMinutes(5), hive1);
        TemperatureReading r3 = new TemperatureReading(29.7, LocalDateTime.now().minusMinutes(7), hive2);

        temperatureReadingRepository.save(r1);
        temperatureReadingRepository.save(r2);
        temperatureReadingRepository.save(r3);

        logger.info("Test data inserted: 2 hives + 3 temperature readings");
    }
}
