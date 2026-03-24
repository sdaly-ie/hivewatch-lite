package com.hivewatch.hivewatchlite;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.hivewatch.hivewatchlite.entity.Hive;
import com.hivewatch.hivewatchlite.service.HiveService;
import com.hivewatch.hivewatchlite.service.TemperatureReadingService;

@Component
@Profile("!test")
public class StartupRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(StartupRunner.class);

    private final HiveService hiveService;
    private final TemperatureReadingService readingService;

    public StartupRunner(HiveService hiveService, TemperatureReadingService readingService) {
        this.hiveService = hiveService;
        this.readingService = readingService;
    }

    @Override
    public void run(String... args) {
        logger.info("Running startup demo data...");

        try {
            Hive hiveA = hiveService.createHive("Hive A", "Back Garden");
            Hive hiveB = hiveService.createHive("Hive B", "Front Garden");

            readingService.recordReading(hiveA.getId(), 34.2, LocalDateTime.now().minusMinutes(10));
            readingService.recordReading(hiveA.getId(), 33.8, LocalDateTime.now().minusMinutes(5));
            readingService.recordReading(hiveB.getId(), 29.7, LocalDateTime.now().minusMinutes(7));

            hiveService.renameHive(hiveA.getId(), "Hive A Queen 2026");
            hiveService.relocateHive(hiveB.getId(), "Apiary Shed");

            double avg = readingService.averageTempLastMinutes(hiveA.getId(), 60);
            logger.info("Hive A average temperature over last 60 minutes: {}", avg);

            readingService.applyOffsetToHive(hiveA.getId(), 0.5);
            logger.info("Applied offset to Hive A readings");

            try {
                readingService.recordNow(hiveA.getId(), 200.0);
            } catch (Exception e) {
                logger.info("Expected validation failure for invalid temperature: {}", e.getMessage());
            }

            try {
                hiveService.deleteById(hiveA.getId());
            } catch (Exception e) {
                logger.info("Expected business-rule failure deleting hive with readings: {}", e.getMessage());
            }

            logger.info("Startup demo complete.");
        } catch (Exception e) {
            logger.warn("Startup demo skipped: {}", e.getMessage());
        }
    }
}