package com.hivewatch.hivewatchlite.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

import com.hivewatch.hivewatchlite.dto.HiveDTO;
import com.hivewatch.hivewatchlite.dto.WriteHiveDTO;
import com.hivewatch.hivewatchlite.entity.Hive;
import com.hivewatch.hivewatchlite.service.HiveNotFoundException;
import com.hivewatch.hivewatchlite.service.HiveService;

@RestController
@RequestMapping("/api/hives")
@CrossOrigin(origins = "http://localhost:5173")
public class HiveController {

    private final HiveService hiveService;

    public HiveController(HiveService hiveService) {
        this.hiveService = hiveService;
    }

    // CRUD
    @PostMapping
    public ResponseEntity<HiveDTO> create(@RequestBody WriteHiveDTO dto) {
        Hive saved = hiveService.createHive(dto.getName(), dto.getLocation());
        return ResponseEntity.status(HttpStatus.CREATED).body(new HiveDTO(saved));
    }

    @GetMapping
    public List<HiveDTO> getAll() {
        List<HiveDTO> dtos = new ArrayList<>();
        for (Hive hive : hiveService.findAll()) {
            dtos.add(new HiveDTO(hive));
        }
        return dtos;
    }

    @GetMapping("/{id}")
    public ResponseEntity<HiveDTO> getById(@PathVariable("id")Long id) {
        Optional<Hive> opt = hiveService.findById(id);
        if (opt.isPresent()) {
            return ResponseEntity.ok(new HiveDTO(opt.get()));
        }
        throw new HiveNotFoundException("Hive with id " + id + " not found");
    }

    @PutMapping("/{id}")
    public HiveDTO update(@PathVariable("id")Long id, @RequestBody WriteHiveDTO updated) {
        Hive hive = new Hive(updated.getName(), updated.getLocation());
        Hive saved = hiveService.update(id, hive);
        return new HiveDTO(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable("id")Long id) {
        hiveService.deleteById(id);
    }

    // Query endpoints (Hive-focused)
    @GetMapping("/by-name")
    public ResponseEntity<HiveDTO> findByNameIgnoreCase(@RequestParam("name") String name) {
        return hiveService.findByNameIgnoreCase(name)
                .map(h -> ResponseEntity.ok(new HiveDTO(h)))
                .orElseThrow(() -> new HiveNotFoundException("Hive with name '" + name + "' not found"));
    }

    @GetMapping("/name-contains")
    public List<HiveDTO> findByNameContainingIgnoreCase(@RequestParam("name") String fragment) {
        List<Hive> results = hiveService.findByNameContainingIgnoreCase(fragment);
        List<HiveDTO> dtos = new ArrayList<>();
        for (Hive hive : results) {
            dtos.add(new HiveDTO(hive));
        }
        return dtos;
    }

    @GetMapping("/location-contains")
    public List<HiveDTO> findByLocationContainingIgnoreCase(@RequestParam("name") String fragment) {
        List<Hive> results = hiveService.findByLocationContainingIgnoreCase(fragment);
        List<HiveDTO> dtos = new ArrayList<>();
        for (Hive hive : results) {
            dtos.add(new HiveDTO(hive));
        }
        return dtos;
    }

    // /api/hives/search?nameFragment=Hive&locationFragment=Garden
    @GetMapping("/search")
    public List<HiveDTO> search(
            @RequestParam(value="nameFragment", required = false) String nameFragment,
            @RequestParam(value="locationFragment", required = false) String locationFragment) {

        boolean hasName = (nameFragment != null && !nameFragment.isBlank());
        boolean hasLocation = (locationFragment != null && !locationFragment.isBlank());

        if (!hasName && !hasLocation) {
            throw new IllegalArgumentException("Provide at least one query parameter: nameFragment or locationFragment");
        }

        List<Hive> results;

        if (hasName && hasLocation) {
            List<Hive> byName = hiveService.findByNameContainingIgnoreCase(nameFragment);
            List<Hive> byLocation = hiveService.findByLocationContainingIgnoreCase(locationFragment);

            Set<Long> locationIds = new HashSet<>();
            for (Hive h : byLocation) {
                locationIds.add(h.getId());
            }

            results = new ArrayList<>();
            for (Hive h : byName) {
                if (locationIds.contains(h.getId())) {
                    results.add(h);
                }
            }

        } else if (hasName) {
            results = hiveService.findByNameContainingIgnoreCase(nameFragment);
        } else {
            results = hiveService.findByLocationContainingIgnoreCase(locationFragment);
        }

        List<HiveDTO> dtos = new ArrayList<>();
        for (Hive hive : results) {
            dtos.add(new HiveDTO(hive));
        }
        return dtos;
    }

    // Business operations (PUT)
    @PutMapping("/{id}/rename")
    public HiveDTO rename(@PathVariable("id")Long id, @RequestParam("name") String name) {
        Hive saved = hiveService.renameHive(id, name);
        return new HiveDTO(saved);
    }

    @PutMapping("/{id}/relocate")
    public HiveDTO relocate(@PathVariable("id")Long id, @RequestParam("location") String location) {
        Hive saved = hiveService.relocateHive(id, location);
        return new HiveDTO(saved);
    }

    // Exception handling (HTTP status and message)
    @ExceptionHandler(HiveNotFoundException.class)
    public ResponseEntity<String> handleNotFound(HiveNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}