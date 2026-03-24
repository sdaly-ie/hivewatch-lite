package com.hivewatch.hivewatchlite.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hivewatch.hivewatchlite.entity.Hive;
import com.hivewatch.hivewatchlite.repo.HiveRepository;
import com.hivewatch.hivewatchlite.repo.TemperatureReadingRepository;

@ExtendWith(MockitoExtension.class)
class HiveServiceImplTest {

    @Mock
    private HiveRepository hiveRepository;

    @Mock
    private TemperatureReadingRepository readingRepository;

    private HiveServiceImpl hiveService;

    @BeforeEach
    void setUp() {
        hiveService = new HiveServiceImpl(hiveRepository, readingRepository);
    }

    @Test
    void createHive_savesNormalizedHiveSuccessfully() {
        when(hiveRepository.findByNameIgnoreCase("Hive Alpha")).thenReturn(Optional.empty());
        when(hiveRepository.save(any(Hive.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Hive result = hiveService.createHive("  Hive Alpha  ", "  North Field  ");

        assertThat(result.getName()).isEqualTo("Hive Alpha");
        assertThat(result.getLocation()).isEqualTo("North Field");

        verify(hiveRepository).findByNameIgnoreCase("Hive Alpha");
        verify(hiveRepository).save(argThat(hive ->
                "Hive Alpha".equals(hive.getName()) &&
                        "North Field".equals(hive.getLocation())));
    }

    @Test
    void createHive_throwsWhenDuplicateNameAttempted() {
        Hive existing = new Hive("Hive Alpha", "Other Location");
        when(hiveRepository.findByNameIgnoreCase("Hive Alpha")).thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> hiveService.createHive("Hive Alpha", "North Field")
        );

        assertThat(ex.getMessage()).contains("unique");
        verify(hiveRepository, never()).save(any(Hive.class));
    }

    @Test
    void deleteById_throwsWhenReadingsStillExistForThatHive() {
        when(hiveRepository.existsById(1L)).thenReturn(true);
        when(readingRepository.countByHive_Id(1L)).thenReturn(2L);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> hiveService.deleteById(1L)
        );

        assertThat(ex.getMessage()).contains("Cannot delete hive");
        verify(hiveRepository, never()).deleteById(1L);
    }
}