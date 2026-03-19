package com.hivewatch.hivewatchlite.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hivewatch.hivewatchlite.dto.TemperatureReadingDTO;
import com.hivewatch.hivewatchlite.dto.WriteTemperatureReadingDTO;
import com.hivewatch.hivewatchlite.entity.Hive;
import com.hivewatch.hivewatchlite.entity.TemperatureReading;
import com.hivewatch.hivewatchlite.service.HiveNotFoundException;
import com.hivewatch.hivewatchlite.service.HiveService;
import com.hivewatch.hivewatchlite.service.TemperatureReadingNotFoundException;
import com.hivewatch.hivewatchlite.service.TemperatureReadingService;

//REST controller that exposes TemperatureReading service features as HTTP endpoints for Postman or clients
@RestController
@RequestMapping("/api/readings")
@CrossOrigin
public class TemperatureReadingController {
	
	// Service that contains the business rules for temperature readings
    private final TemperatureReadingService readingService;
    
    // Service used to confirm a hive exists when updating/creating readings
    private final HiveService hiveService;
    
    // Spring injects the services so this controller can delegate work to the service layer
    public TemperatureReadingController(TemperatureReadingService readingService, HiveService hiveService) {
        this.readingService = readingService;
        this.hiveService = hiveService;
    }

    // POST Create - Create a new reading (uses recordNow if recordedAt is missing, otherwise recordReading)
    @PostMapping
    public ResponseEntity<TemperatureReadingDTO> create(@RequestBody WriteTemperatureReadingDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Request body is required");
        if (dto.getHiveId() == null) throw new IllegalArgumentException("hiveId is required");
        if (dto.getTemperature() == null) throw new IllegalArgumentException("temperature is required");

        TemperatureReading saved = (dto.getRecordedAt() == null)
                ? readingService.recordNow(dto.getHiveId(), dto.getTemperature())
                : readingService.recordReading(dto.getHiveId(), dto.getTemperature(), dto.getRecordedAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(new TemperatureReadingDTO(saved));
    }

    // GET all - Return all readings as DTO's (in other words as safe data for clients)
    @GetMapping
    public List<TemperatureReadingDTO> getAll() {
        List<TemperatureReadingDTO> dtos = new ArrayList<>();
        for (TemperatureReading r : readingService.findAll()) {
            dtos.add(new TemperatureReadingDTO(r));
        }
        return dtos;
    }

    // GET by id - Return one reading by id or 404 if it does not exist
    @GetMapping("/{id}")
    public ResponseEntity<TemperatureReadingDTO> getById(@PathVariable("id") Long id) {
        Optional<TemperatureReading> opt = readingService.findById(id);
        if (opt.isPresent()) return ResponseEntity.ok(new TemperatureReadingDTO(opt.get()));
        throw new TemperatureReadingNotFoundException("Reading with id " + id + " not found");
    }

    // PUT update - Update an existing reading using validated input and an existing hive reference
    @PutMapping("/{id}")
    public TemperatureReadingDTO update(@PathVariable("id") Long id, @RequestBody WriteTemperatureReadingDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Request body is required");
        if (dto.getHiveId() == null) throw new IllegalArgumentException("hiveId is required for update");
        if (dto.getTemperature() == null) throw new IllegalArgumentException("temperature is required for update");
        if (dto.getRecordedAt() == null) throw new IllegalArgumentException("recordedAt is required for update");

        Hive hive = hiveService.findById(dto.getHiveId())
                .orElseThrow(() -> new HiveNotFoundException("Hive with id " + dto.getHiveId() + " not found"));

        TemperatureReading updated = new TemperatureReading(dto.getTemperature(), dto.getRecordedAt(), hive);
        TemperatureReading saved = readingService.update(id, updated);
        return new TemperatureReadingDTO(saved);
    }

    // DELETE - Delete a reading by id (returns 204 if deleted)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable("id") Long id) {
        readingService.deleteById(id);
    }

    // Query 1 - Return all readings for a hive, newest first
    @GetMapping("/hive/{hiveId}")
    public List<TemperatureReadingDTO> byHive(@PathVariable("hiveId") Long hiveId) {
        List<TemperatureReadingDTO> dtos = new ArrayList<>();
        for (TemperatureReading r : readingService.findByHiveIdOrderByRecordedAtDesc(hiveId)) {
            dtos.add(new TemperatureReadingDTO(r));
        }
        return dtos;
    }

    // Query 2 - Return the most recent reading for a hive (or 204 if none exist)
    @GetMapping("/hive/{hiveId}/latest")
    public ResponseEntity<TemperatureReadingDTO> latest(@PathVariable("hiveId") Long hiveId) {
        return readingService.findLatestForHive(hiveId)
                .map(r -> ResponseEntity.ok(new TemperatureReadingDTO(r)))
                .orElse(ResponseEntity.noContent().build());
    }

    // Query 3 - Return readings for a hive between two timestamps
    @GetMapping("/hive/{hiveId}/between")
    public List<TemperatureReadingDTO> between(
            @PathVariable("hiveId") Long hiveId,
            @RequestParam("start") @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime end) {

        List<TemperatureReadingDTO> dtos = new ArrayList<>();
        for (TemperatureReading r : readingService.findByHiveIdBetween(hiveId, start, end)) {
            dtos.add(new TemperatureReadingDTO(r));
        }
        return dtos;
    }

    // Query 4 - Return the number of readings stored for a hive
    @GetMapping("/hive/{hiveId}/count")
    public long count(@PathVariable("hiveId") Long hiveId) {
        return readingService.countByHiveId(hiveId);
    }

    // Business Query - Return the average temperature for readings in the last N minutes
    @GetMapping("/hive/{hiveId}/average-last-minutes")
    public double averageLastMinutes(@PathVariable("hiveId") Long hiveId, @RequestParam("minutes") int minutes) {
        return readingService.averageTempLastMinutes(hiveId, minutes);
    }

    // Relationship PUT - Move a reading to a different hive (to re-assign ownership)
    @PutMapping("/{readingId}/assign-hive/{hiveId}")
    public TemperatureReadingDTO assignToHive(
            @PathVariable("readingId") Long readingId,
            @PathVariable("hiveId") Long hiveId) {
        TemperatureReading saved = readingService.assignToHive(readingId, hiveId);
        return new TemperatureReadingDTO(saved);
    }

    // Business PUT - Apply a calibration offset to all readings for a hive and return how many changed
    @PutMapping("/hive/{hiveId}/apply-offset")
    public int applyOffset(@PathVariable("hiveId") Long hiveId, @RequestParam("delta") double delta) {
        return readingService.applyOffsetToHive(hiveId, delta);
    }

    // Exception handling - Return 404 when a reading cannot be found
    @ExceptionHandler(TemperatureReadingNotFoundException.class)
    public ResponseEntity<String> handleNotFound(TemperatureReadingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    // Exception handling - Return 404 when a hive cannot be found
    @ExceptionHandler(HiveNotFoundException.class)
    public ResponseEntity<String> handleHiveNotFound(HiveNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    // Exception handling - Return 400 when the request data is missing/invalid
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    // Exception handling - Return 409 when a business rule blocks the request (such as duplicates or conflicts)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}