package com.hivewatch.hivewatchlite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HivewatchliteApplication {

    private static final Logger logger = LoggerFactory.getLogger(HivewatchliteApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(HivewatchliteApplication.class, args);
        logger.info("HiveWatch Lite started");
    }
}