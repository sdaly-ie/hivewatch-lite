package com.hivewatch.hivewatchlite;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.hivewatch.hivewatchlite.entity.Hive;
import com.hivewatch.hivewatchlite.entity.TemperatureReading;
import com.hivewatch.hivewatchlite.service.HiveService;
import com.hivewatch.hivewatchlite.service.TemperatureReadingService;

@SpringBootApplication
public class HivewatchliteApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(HivewatchliteApplication.class);

    private final HiveService hiveService;
    private final TemperatureReadingService readingService;

    public HivewatchliteApplication(HiveService hiveService, TemperatureReadingService readingService) {
        this.hiveService = hiveService;
        this.readingService = readingService;
    }

    public static void main(String[] args) {
        SpringApplication.run(HivewatchliteApplication.class, args);
        logger.info("HiveWatch Lite started");
    }

    @Override
    public void run(String... args) {

        // Create hives
        Hive hiveA = hiveService.createHive("Hive A", "Back Garden");
        Hive hiveB = hiveService.createHive("Hive B", "Front Garden");

        logger.info("Created hives: A id={}, B id={}", hiveA.getId(), hiveB.getId());

        // Record readings but with temperature bounds, no future timestamps and no duplicates
        TemperatureReading r1 = readingService.recordReading(hiveA.getId(), 34.2, LocalDateTime.now().minusMinutes(10));
        TemperatureReading r2 = readingService.recordReading(hiveA.getId(), 33.8, LocalDateTime.now().minusMinutes(5));
        TemperatureReading r3 = readingService.recordReading(hiveB.getId(), 29.7, LocalDateTime.now().minusMinutes(7));

        logger.info("Inserted readings: r1 id={}, r2 id={}, r3 id={}", r1.getId(), r2.getId(), r3.getId());

        // Query demos
        logger.info("Find by name contains 'Hive': count={}", hiveService.findByNameContainingIgnoreCase("Hive").size());
        logger.info("Find by location contains 'Garden': count={}", hiveService.findByLocationContainingIgnoreCase("Garden").size());

        logger.info("Hive A reading count={}", readingService.countByHiveId(hiveA.getId()));
        logger.info("Hive A latest reading id={}",
                readingService.findLatestForHive(hiveA.getId()).map(TemperatureReading::getId).orElse(null));

        // Business updates
        hiveService.relocateHive(hiveB.getId(), "Apiary Shed");
        hiveService.renameHive(hiveA.getId(), "Hive A (Queen 2026)");

        logger.info("Updated hive names/locations via service layer.");

        // Business calculation
        double avg10 = readingService.averageTempLastMinutes(hiveA.getId(), 60);
        logger.info("Hive A avg temp last 60 mins={}", avg10);

        // Validation demo
        try {
            readingService.recordNow(hiveA.getId(), 200.0);
        } catch (Exception e) {
            logger.info("Expected validation failure (bad temperature): {}", e.getMessage());
        }

        // Deletion rule demo, you cannot delete hive with readings
        try {
            hiveService.deleteById(hiveA.getId());
        } catch (Exception e) {
            logger.info("Expected business-rule failure (cannot delete hive with readings): {}", e.getMessage());
        }

        logger.info("Service layer demo complete (H2 in-memory).");
    }
}