package com.example.training_project.service;

import com.example.training_project.dto.WorkoutWithExercisesRequest;
import com.example.training_project.entity.Athlete;
import com.example.training_project.entity.TrainingProgram;
import com.example.training_project.repository.AthleteRepository;
import com.example.training_project.repository.ExerciseRepository;
import com.example.training_project.repository.TrainingProgramRepository;
import com.example.training_project.repository.WorkoutRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class WorkoutBulkTransactionIntegrationTest {

    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private AthleteRepository athleteRepository;

    @Autowired
    private TrainingProgramRepository trainingProgramRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Test
    void transactionalBulkRollsBackAllChangesOnFailure() {
        Athlete athlete = athleteRepository.findAll().get(0);
        TrainingProgram program = trainingProgramRepository.findAll().get(0);
        long workoutsBefore = workoutRepository.count();
        long exercisesBefore = exerciseRepository.count();

        List<WorkoutWithExercisesRequest> requests = List.of(
                request("TX-OK-" + UUID.randomUUID(), athlete.getId(), program.getId(), List.of("TX_EX_1")),
                request("FAIL", athlete.getId(), program.getId(), List.of("TX_EX_FAIL"))
        );

        assertThatThrownBy(() -> workoutService.addWorkoutsWithExercisesBulkTransactional(requests))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Simulated failure");

        assertThat(workoutRepository.count()).isEqualTo(workoutsBefore);
        assertThat(exerciseRepository.count()).isEqualTo(exercisesBefore);
    }

    @Test
    void nonTransactionalBulkLeavesPartialDataOnFailure() {
        Athlete athlete = athleteRepository.findAll().get(0);
        TrainingProgram program = trainingProgramRepository.findAll().get(0);
        long workoutsBefore = workoutRepository.count();
        long exercisesBefore = exerciseRepository.count();

        List<WorkoutWithExercisesRequest> requests = List.of(
                request("NON-TX-OK-" + UUID.randomUUID(), athlete.getId(), program.getId(), List.of("NON_TX_EX_1")),
                request("FAIL", athlete.getId(), program.getId(), List.of("NON_TX_EX_FAIL"))
        );

        assertThatThrownBy(() -> workoutService.addWorkoutsWithExercisesBulkNonTransactional(requests))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Simulated failure");

        assertThat(workoutRepository.count()).isEqualTo(workoutsBefore + 1);
        assertThat(exerciseRepository.count()).isEqualTo(exercisesBefore + 2);
    }

    private WorkoutWithExercisesRequest request(final String title,
                                                final Long athleteId,
                                                final Long programId,
                                                final List<String> exerciseNames) {
        return new WorkoutWithExercisesRequest(
                title,
                "Strength",
                50,
                LocalDateTime.now().plusDays(3),
                athleteId,
                programId,
                exerciseNames
        );
    }
}
