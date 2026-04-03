package com.example.training_project;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCoverCrudFlowsForCoreResources() throws Exception {
        Long coachId = createCoach("Pavel", "Ivanov");
        Long athleteId = createAthlete("Maksim", "Sidorov", coachId);
        Long programId = createProgram("Cutting Plan");
        Long exerciseId = createExercise("Pull Up");

        mockMvc.perform(get("/api/coaches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());

        mockMvc.perform(get("/api/coaches/{id}", coachId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Pavel"));

        mockMvc.perform(put("/api/coaches/{id}", coachId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("firstName", "Petr", "lastName", "Ivanov"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Petr"));

        mockMvc.perform(get("/api/athletes/{id}", athleteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coachId").value(coachId));

        mockMvc.perform(put("/api/athletes/{id}", athleteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Maksim",
                                "lastName", "Sidorov",
                                "coachId", coachId
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Maksim"));

        mockMvc.perform(get("/api/programs/{id}", programId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cutting Plan"));

        mockMvc.perform(put("/api/programs/{id}", programId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Strength Block"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Strength Block"));

        mockMvc.perform(get("/api/exercises/{id}", exerciseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pull Up"));

        mockMvc.perform(put("/api/exercises/{id}", exerciseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Pull Down"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pull Down"));

        Long workoutId = createWorkout("Tempo Day", "Cardio", athleteId, programId, List.of(exerciseId));

        mockMvc.perform(get("/api/workouts/{id}", workoutId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Tempo Day"));

        mockMvc.perform(get("/api/workouts").param("type", "cardio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());

        mockMvc.perform(get("/api/workouts/search/jpql")
                        .param("type", "cardio")
                        .param("coachName", "Petr Ivanov")
                        .param("programName", "Strength Block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(get("/api/workouts/search/native")
                        .param("coachName", "Petr Ivanov")
                        .param("programName", "Strength Block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(get("/api/workouts/optimized"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());

        mockMvc.perform(get("/api/workouts/persisted_counts"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/workouts/{id}", workoutId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Tempo Day Updated",
                                "type", "Cardio",
                                "durationMinutes", 65,
                                "scheduledAt", LocalDateTime.now().plusDays(3).toString(),
                                "athleteId", athleteId,
                                "programId", programId,
                                "exerciseIds", List.of(exerciseId)
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Tempo Day Updated"));

        mockMvc.perform(delete("/api/workouts/{id}", workoutId))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/exercises/{id}", exerciseId)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/programs/{id}", programId)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/athletes/{id}", athleteId)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/coaches/{id}", coachId)).andExpect(status().isNoContent());
    }

    @Test
    void shouldCoverErrorScenariosAndBulkWorkoutEndpoints() throws Exception {
        mockMvc.perform(get("/api/workouts/invalid-id"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));

        mockMvc.perform(get("/api/coaches/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));

        Long coachId = createCoach("Andrey", "Romanov");
        Long athleteId = createAthlete("Nikita", "Romanov", coachId);
        Long programId = createProgram("Hypertrophy Plan");

        mockMvc.perform(post("/api/coaches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("firstName", "Andrey", "lastName", "Romanov"))))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/workouts/with_exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Upper Flow",
                                "type", "Hypertrophy",
                                "durationMinutes", 50,
                                "scheduledAt", LocalDateTime.now().plusDays(2).toString(),
                                "athleteId", athleteId,
                                "programId", programId,
                                "exerciseNames", List.of("Fly", "Press")
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Upper Flow"));

        String bulkResponse = mockMvc.perform(post("/api/workouts/with_exercises/bulk/transactional")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(
                                Map.of(
                                        "title", "Bulk A",
                                        "type", "Hypertrophy",
                                        "durationMinutes", 40,
                                        "scheduledAt", LocalDateTime.now().plusDays(4).toString(),
                                        "athleteId", athleteId,
                                        "programId", programId,
                                        "exerciseNames", List.of("Plank")
                                )
                        ))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(bulkResponse);
        assertThat(jsonNode).hasSize(1);

        mockMvc.perform(post("/api/workouts/with_exercises/bulk/non_transactional")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(
                                Map.of(
                                        "title", "Bulk B",
                                        "type", "Hypertrophy",
                                        "durationMinutes", 40,
                                        "scheduledAt", LocalDateTime.now().plusDays(5).toString(),
                                        "athleteId", athleteId,
                                        "programId", programId,
                                        "exerciseNames", List.of("Crunch")
                                )
                        ))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/workouts/with_exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "FAIL",
                                "type", "Hypertrophy",
                                "durationMinutes", 50,
                                "scheduledAt", LocalDateTime.now().plusDays(2).toString(),
                                "athleteId", athleteId,
                                "programId", programId,
                                "exerciseNames", List.of("Face Pull")
                        ))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    private Long createCoach(final String firstName, final String lastName) throws Exception {
        String response = mockMvc.perform(post("/api/coaches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("firstName", firstName, "lastName", lastName))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long createAthlete(final String firstName, final String lastName, final Long coachId) throws Exception {
        String response = mockMvc.perform(post("/api/athletes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", firstName,
                                "lastName", lastName,
                                "coachId", coachId
                        ))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long createProgram(final String name) throws Exception {
        String response = mockMvc.perform(post("/api/programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", name))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long createExercise(final String name) throws Exception {
        String response = mockMvc.perform(post("/api/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", name))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long createWorkout(final String title,
                               final String type,
                               final Long athleteId,
                               final Long programId,
                               final List<Long> exerciseIds) throws Exception {
        String response = mockMvc.perform(post("/api/workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", title,
                                "type", type,
                                "durationMinutes", 60,
                                "scheduledAt", LocalDateTime.now().plusDays(2).toString(),
                                "athleteId", athleteId,
                                "programId", programId,
                                "exerciseIds", exerciseIds
                        ))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }
}
