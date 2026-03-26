package com.hivewatch.hivewatchlite.controller;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "application", "HiveWatch Lite API",
                "status", "running",
                "timestamp", Instant.now().toString(),
                "message", "HiveWatch Lite API is running.",
                "routes", List.of(
                        "/api/hives",
                        "/api/readings",
                        "/h2-console"
                )
        );
    }
}