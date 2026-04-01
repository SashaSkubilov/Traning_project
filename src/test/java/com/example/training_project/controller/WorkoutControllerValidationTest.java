package com.example.training_project.controller;

import com.example.training_project.dto.WorkoutCreateUpdateRequest;
import com.example.training_project.exception.DuplicateResourceException;
import com.example.training_project.service.WorkoutService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkoutController.class)
class WorkoutControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WorkoutService workoutService;

    @Test
    void shouldReturnUnified400ErrorForInvalidRequest() throws Exception {
        WorkoutCreateUpdateRequest request = new WorkoutCreateUpdateRequest(
                "",
                "",
                0,
                LocalDateTime.now().minusDays(1),
                null,
                null,
                List.of()
        );

        mockMvc.perform(post("/api/workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/workouts"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void shouldReturnUnified409ErrorForDuplicateWorkout() throws Exception {
        WorkoutCreateUpdateRequest request = new WorkoutCreateUpdateRequest(
                "Leg Day",
                "Strength",
                70,
                LocalDateTime.now().plusDays(2),
                1L,
                1L,
                List.of(1L)
        );

        doThrow(new DuplicateResourceException("Workout already exists"))
                .when(workoutService)
                .createWorkout(any(WorkoutCreateUpdateRequest.class));

        mockMvc.perform(post("/api/workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Workout already exists"));
    }
}
