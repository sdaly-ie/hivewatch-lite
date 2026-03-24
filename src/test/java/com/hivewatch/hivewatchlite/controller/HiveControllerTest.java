package com.hivewatch.hivewatchlite.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.hivewatch.hivewatchlite.entity.Hive;
import com.hivewatch.hivewatchlite.service.HiveService;

@WebMvcTest(HiveController.class)
class HiveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HiveService hiveService;

    @Test
    void create_returns201Created_forValidInput() throws Exception {
        Hive saved = new Hive("Hive Alpha", "North Field");
        ReflectionTestUtils.setField(saved, "id", 1L);

        when(hiveService.createHive(eq("Hive Alpha"), eq("North Field"))).thenReturn(saved);

        String json = """
                {
                  "name": "Hive Alpha",
                  "location": "North Field"
                }
                """;

        mockMvc.perform(post("/api/hives")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Hive Alpha"))
                .andExpect(jsonPath("$.location").value("North Field"));
    }

    @Test
    void search_returns400BadRequest_whenNoQueryParamsSupplied() throws Exception {
        mockMvc.perform(get("/api/hives/search"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Provide at least one query parameter: nameFragment or locationFragment"));
    }

    @Test
    void deleteById_returns409Conflict_whenServiceThrowsIllegalStateException() throws Exception {
        doThrow(new IllegalStateException("Cannot delete hive with readings"))
                .when(hiveService).deleteById(1L);

        mockMvc.perform(delete("/api/hives/1"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Cannot delete hive with readings"));
    }
}