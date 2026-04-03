package com.example.training_project.controller;

import com.example.training_project.dto.AthleteCreateUpdateRequest;
import com.example.training_project.dto.CoachCreateUpdateRequest;
import com.example.training_project.dto.ExerciseCreateUpdateRequest;
import com.example.training_project.dto.TrainingProgramCreateUpdateRequest;
import com.example.training_project.dto.WorkoutCreateUpdateRequest;
import com.example.training_project.dto.WorkoutDto;
import com.example.training_project.dto.WorkoutWithExercisesRequest;
import com.example.training_project.service.AthleteService;
import com.example.training_project.service.CoachService;
import com.example.training_project.service.ExerciseService;
import com.example.training_project.service.TrainingProgramService;
import com.example.training_project.service.WorkoutService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControllersSmokeUnitTest {

    @Mock
    private AthleteService athleteService;
    @Mock
    private CoachService coachService;
    @Mock
    private ExerciseService exerciseService;
    @Mock
    private TrainingProgramService trainingProgramService;
    @Mock
    private WorkoutService workoutService;

    @Test
    void shouldCallCrudControllersMethods() {
        AthleteController athleteController = new AthleteController(athleteService);
        CoachController coachController = new CoachController(coachService);
        ExerciseController exerciseController = new ExerciseController(exerciseService);
        TrainingProgramController programController = new TrainingProgramController(trainingProgramService);

        AthleteCreateUpdateRequest athleteRequest = new AthleteCreateUpdateRequest("A", "B", null);
        CoachCreateUpdateRequest coachRequest = new CoachCreateUpdateRequest("C", "D");
        ExerciseCreateUpdateRequest exerciseRequest = new ExerciseCreateUpdateRequest("Row");
        TrainingProgramCreateUpdateRequest programRequest = new TrainingProgramCreateUpdateRequest("Mass");

        athleteController.getAll();
        athleteController.getById(1L);
        athleteController.create(athleteRequest);
        athleteController.update(1L, athleteRequest);
        athleteController.delete(1L);

        coachController.getAll();
        coachController.getById(1L);
        coachController.create(coachRequest);
        coachController.update(1L, coachRequest);
        coachController.delete(1L);

        exerciseController.getAll();
        exerciseController.getById(1L);
        exerciseController.create(exerciseRequest);
        exerciseController.update(1L, exerciseRequest);
        exerciseController.delete(1L);

        programController.getAll();
        programController.getById(1L);
        programController.create(programRequest);
        programController.update(1L, programRequest);
        programController.delete(1L);

        verify(athleteService).delete(1L);
        verify(coachService).delete(1L);
        verify(exerciseService).delete(1L);
        verify(trainingProgramService).delete(1L);
    }

    @Test
    void shouldCallWorkoutControllerMethods() {
        WorkoutController workoutController = new WorkoutController(workoutService);

        WorkoutCreateUpdateRequest createUpdateRequest = new WorkoutCreateUpdateRequest(
                "Title", "Type", 30, LocalDateTime.now().plusDays(1), 1L, 1L, List.of(1L)
        );
        WorkoutWithExercisesRequest withExercisesRequest = new WorkoutWithExercisesRequest(
                "Title", "Type", 30, LocalDateTime.now().plusDays(1), 1L, 1L, List.of("Row")
        );

        when(workoutService.searchWorkoutsJpql(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(workoutService.searchWorkoutsNative(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(workoutService.addWorkoutWithExercises(any())).thenReturn(new WorkoutDto(
                1L, "Title", "Type", 30, LocalDateTime.now(), "Athlete", "Program", 1
        ));

        workoutController.getAll(null);
        workoutController.searchJpql(null, null, null, PageRequest.of(0, 10));
        workoutController.searchNative(null, null, PageRequest.of(0, 10));
        workoutController.getAllOptimized();
        workoutController.getById(1L);
        workoutController.create(createUpdateRequest);
        workoutController.update(1L, createUpdateRequest);
        workoutController.delete(1L);
        workoutController.getPersistedCounts();
        assertThat(workoutController.addWorkoutWithExercises(withExercisesRequest).getStatusCode().value())
                .isEqualTo(201);
        workoutController.addWorkoutsWithExercisesBulkTransactional(List.of(withExercisesRequest));
        workoutController.addWorkoutsWithExercisesBulkNonTransactional(List.of(withExercisesRequest));

        verify(workoutService).deleteWorkout(1L);
        verify(workoutService).getPersistedCounts();
    }
}
