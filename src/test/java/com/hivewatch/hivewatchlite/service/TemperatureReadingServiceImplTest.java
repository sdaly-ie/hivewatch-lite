package com.hivewatch.hivewatchlite.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hivewatch.hivewatchlite.entity.Hive;
import com.hivewatch.hivewatchlite.entity.TemperatureReading;
import com.hivewatch.hivewatchlite.repo.HiveRepository;
import com.hivewatch.hivewatchlite.repo.TemperatureReadingRepository;

@ExtendWith(MockitoExtension.class)
class TemperatureReadingServiceImplTest {

    @Mock
    private TemperatureReadingRepository readingRepository;

    @Mock
    private HiveRepository hiveRepository;

    private TemperatureReadingServiceImpl readingService;

    @BeforeEach
    void setUp() {
        readingService = new TemperatureReadingServiceImpl(readingRepository, hiveRepository);
    }

    @Test
    void recordReading_throwsWhenRecordedAtIsNull() {
        when(hiveRepository.existsById(1L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> readingService.recordReading(1L, 34.2, null)
        );

        assertThat(ex.getMessage()).contains("recordedAt is required");
        verify(readingRepository, never()).save(any(TemperatureReading.class));
    }

    @Test
    void assignToHive_throwsWhenTargetHiveAlreadyHasReadingAtSameTimestamp() {
        LocalDateTime recordedAt = LocalDateTime.of(2026, 3, 24, 12, 0);

        Hive currentHive = new Hive("Hive A", "Back Garden");
        TemperatureReading existingReading = new TemperatureReading(34.5, recordedAt, currentHive);

        when(hiveRepository.existsById(2L)).thenReturn(true);
        when(readingRepository.findById(10L)).thenReturn(Optional.of(existingReading));
        when(readingRepository.existsByHive_IdAndRecordedAt(2L, recordedAt)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> readingService.assignToHive(10L, 2L)
        );

        assertThat(ex.getMessage()).contains("Cannot assign reading");
        verify(readingRepository, never()).save(any(TemperatureReading.class));
        verify(hiveRepository, never()).findById(2L);
    }

    @Test
    void averageTempLastMinutes_throwsForInvalidMinuteInput() {
        when(hiveRepository.existsById(1L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> readingService.averageTempLastMinutes(1L, 0)
        );

        assertThat(ex.getMessage()).contains("minutes");
        verify(readingRepository, never()).findAverageTempSince(anyLong(), any());
    }
}