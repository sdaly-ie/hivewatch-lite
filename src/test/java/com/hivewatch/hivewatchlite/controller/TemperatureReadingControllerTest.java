package com.hivewatch.hivewatchlite.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.hivewatch.hivewatchlite.entity.Hive;
import com.hivewatch.hivewatchlite.entity.TemperatureReading;
import com.hivewatch.hivewatchlite.service.HiveService;
import com.hivewatch.hivewatchlite.service.TemperatureReadingService;

@WebMvcTest(TemperatureReadingController.class)
class TemperatureReadingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TemperatureReadingService readingService;

    @MockBean
    private HiveService hiveService;

    @Test
    void create_returns201Created_forValidInput() throws Exception {
        LocalDateTime recordedAt = LocalDateTime.of(2026, 3, 24, 12, 0);

        Hive hive = new Hive("Hive Alpha", "North Field");
        ReflectionTestUtils.setField(hive, "id", 1L);

        TemperatureReading saved = new TemperatureReading(34.2, recordedAt, hive);
        ReflectionTestUtils.setField(saved, "id", 10L);

        when(readingService.recordReading(eq(1L), eq(34.2), eq(recordedAt))).thenReturn(saved);

        String json = """
                {
                  "temperature": 34.2,
                  "recordedAt": "2026-03-24T12:00:00",
                  "hiveId": 1
                }
                """;

        mockMvc.perform(post("/api/readings")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.temperature").value(34.2))
                .andExpect(jsonPath("$.recordedAt").value("2026-03-24T12:00:00"))
                .andExpect(jsonPath("$.hiveId").value(1))
                .andExpect(jsonPath("$.hiveName").value("Hive Alpha"));
    }

    @Test
    void assignToHive_returns200Ok_andBodyForValidReassignment() throws Exception {
        LocalDateTime recordedAt = LocalDateTime.of(2026, 3, 24, 12, 0);

        Hive targetHive = new Hive("Hive Beta", "South Field");
        ReflectionTestUtils.setField(targetHive, "id", 2L);

        TemperatureReading reassigned = new TemperatureReading(31.5, recordedAt, targetHive);
        ReflectionTestUtils.setField(reassigned, "id", 10L);

        when(readingService.assignToHive(10L, 2L)).thenReturn(reassigned);

        mockMvc.perform(put("/api/readings/10/assign-hive/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.temperature").value(31.5))
                .andExpect(jsonPath("$.recordedAt").value("2026-03-24T12:00:00"))
                .andExpect(jsonPath("$.hiveId").value(2))
                .andExpect(jsonPath("$.hiveName").value("Hive Beta"));
    }

    @Test
    void applyOffset_returns400BadRequest_whenServiceThrowsIllegalArgumentException() throws Exception {
        when(readingService.applyOffsetToHive(1L, 1.5))
                .thenThrow(new IllegalArgumentException("delta is invalid"));

        mockMvc.perform(put("/api/readings/hive/1/apply-offset")
                        .param("delta", "1.5"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("delta is invalid"));
    }
}