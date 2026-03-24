package com.hivewatch.hivewatchlite.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
        ReflectionTestUtils.setField(currentHive, "id", 1L);

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

    @ParameterizedTest
    @ValueSource(doubles = {-9.1, 46.6})
    void recordReading_throwsWhenTemperatureOutsideBounds(double temperature) {
        LocalDateTime recordedAt = LocalDateTime.of(2026, 3, 24, 12, 0);
        Hive hive = persistedHive(1L, "Hive Alpha", "North Field");

        when(hiveRepository.existsById(1L)).thenReturn(true);
        when(hiveRepository.findById(1L)).thenReturn(Optional.of(hive));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> readingService.recordReading(1L, temperature, recordedAt)
        );

        assertThat(ex.getMessage()).contains("Temperature must be between");
        verify(readingRepository, never()).save(any(TemperatureReading.class));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-9.0, 46.5})
    void recordReading_acceptsBoundaryTemperatures(double temperature) {
        LocalDateTime recordedAt = LocalDateTime.of(2026, 3, 24, 12, 0);
        Hive hive = persistedHive(1L, "Hive Alpha", "North Field");

        when(hiveRepository.existsById(1L)).thenReturn(true);
        when(hiveRepository.findById(1L)).thenReturn(Optional.of(hive));
        when(readingRepository.existsByHive_IdAndRecordedAt(1L, recordedAt)).thenReturn(false);
        when(readingRepository.save(any(TemperatureReading.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TemperatureReading result = readingService.recordReading(1L, temperature, recordedAt);

        assertThat(result.getTemperature()).isEqualTo(temperature);
        assertThat(result.getRecordedAt()).isEqualTo(recordedAt);
        assertThat(result.getHive().getId()).isEqualTo(1L);
        verify(readingRepository).save(any(TemperatureReading.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1441})
    void averageTempLastMinutes_throwsWhenMinutesOutsideBounds(int minutes) {
        when(hiveRepository.existsById(1L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> readingService.averageTempLastMinutes(1L, minutes)
        );

        assertThat(ex.getMessage()).contains("minutes");
        verify(readingRepository, never()).findAverageTempSince(anyLong(), any());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 1440})
    void averageTempLastMinutes_acceptsBoundaryMinuteValues(int minutes) {
        when(hiveRepository.existsById(1L)).thenReturn(true);
        when(readingRepository.findAverageTempSince(eq(1L), any(LocalDateTime.class)))
                .thenReturn(30.0);

        double result = readingService.averageTempLastMinutes(1L, minutes);

        assertThat(result).isEqualTo(30.0);
        verify(readingRepository).findAverageTempSince(eq(1L), any(LocalDateTime.class));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-20.1, 20.1})
    void applyOffsetToHive_throwsWhenDeltaOutsideBounds(double delta) {
        when(hiveRepository.existsById(1L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> readingService.applyOffsetToHive(1L, delta)
        );

        assertThat(ex.getMessage()).contains("delta");
        verify(readingRepository, never()).applyOffsetToHive(1L, delta);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-20.0, 20.0})
    void applyOffsetToHive_acceptsBoundaryDeltaValues(double delta) {
        when(hiveRepository.existsById(1L)).thenReturn(true);
        when(readingRepository.applyOffsetToHive(1L, delta)).thenReturn(2);

        int updatedCount = readingService.applyOffsetToHive(1L, delta);

        assertThat(updatedCount).isEqualTo(2);
        verify(readingRepository).applyOffsetToHive(1L, delta);
    }

    private Hive persistedHive(Long id, String name, String location) {
        Hive hive = new Hive(name, location);
        ReflectionTestUtils.setField(hive, "id", id);
        return hive;
    }
}