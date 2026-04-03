package com.example.training_project.service;

import com.example.training_project.dto.WorkoutCreateUpdateRequest;
import com.example.training_project.dto.WorkoutWithExercisesRequest;
import com.example.training_project.entity.Athlete;
import com.example.training_project.entity.TrainingProgram;
import com.example.training_project.entity.Workout;
import com.example.training_project.exception.DuplicateResourceException;
import com.example.training_project.mapper.WorkoutMapper;
import com.example.training_project.repository.AthleteRepository;
import com.example.training_project.repository.CoachRepository;
import com.example.training_project.repository.ExerciseRepository;
import com.example.training_project.repository.TrainingProgramRepository;
import com.example.training_project.repository.WorkoutRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceAdditionalBranchUnitTest {

    @Mock
    private WorkoutRepository workoutRepository;
    @Mock
    private AthleteRepository athleteRepository;
    @Mock
    private TrainingProgramRepository trainingProgramRepository;
    @Mock
    private ExerciseRepository exerciseRepository;
    @Mock
    private CoachRepository coachRepository;

    private WorkoutService workoutService;

    @BeforeEach
    void setUp() {
        workoutService = new WorkoutService(
                workoutRepository,
                athleteRepository,
                trainingProgramRepository,
                exerciseRepository,
                coachRepository,
                new WorkoutMapper()
        );
    }

    @Test
    void shouldCoverSimpleGettersAndNotFoundPath() {
        Workout workout = new Workout();
        workout.setTitle("Leg Day");
        workout.setType("Strength");
        when(workoutRepository.findAll()).thenReturn(List.of(workout));
        assertThat(workoutService.getAllWorkouts()).hasSize(1);
        assertThat(workoutService.getWorkouts("strength")).hasSize(1);

        when(workoutRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> workoutService.getWorkoutById(99L)).isInstanceOf(EntityNotFoundException.class);

        when(workoutRepository.findByFiltersNative(anyString(), anyString(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        workoutService.searchWorkoutsNative("coach", "program", PageRequest.of(0, 10));
        workoutService.searchWorkoutsNative("coach", "program", PageRequest.of(0, 10));
    }

    @Test
    void shouldCoverCreateUpdateAndExerciseValidationBranches() {
        WorkoutCreateUpdateRequest request = new WorkoutCreateUpdateRequest(
                "Title", "Type", 45, LocalDateTime.now().plusDays(1), 1L, 1L, List.of(1L)
        );

        when(workoutRepository.existsByTitleIgnoreCaseAndScheduledAt(anyString(), any())).thenReturn(true);
        assertThatThrownBy(() -> workoutService.createWorkout(request)).isInstanceOf(DuplicateResourceException.class);

        when(workoutRepository.existsByTitleIgnoreCaseAndScheduledAtAndIdNot(anyString(), any(), anyLong())).thenReturn(true);
        assertThatThrownBy(() -> workoutService.updateWorkout(1L, request)).isInstanceOf(DuplicateResourceException.class);

        when(workoutRepository.existsByTitleIgnoreCaseAndScheduledAtAndIdNot(anyString(), any(), anyLong())).thenReturn(false);
        when(workoutRepository.findById(1L)).thenReturn(Optional.of(new Workout()));
        when(athleteRepository.findById(1L)).thenReturn(Optional.of(new Athlete()));
        when(trainingProgramRepository.findById(1L)).thenReturn(Optional.of(new TrainingProgram("P")));
        when(exerciseRepository.findAllById(any())).thenReturn(List.of());
        assertThatThrownBy(() -> workoutService.updateWorkout(1L, request)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldCoverBulkNormalizationAndExerciseNameValidation() {
        assertThatThrownBy(() -> workoutService.addWorkoutsWithExercisesBulkTransactional(List.of()))
                .isInstanceOf(IllegalArgumentException.class);

        List<WorkoutWithExercisesRequest> withNullItem = new ArrayList<>();
        withNullItem.add(null);
        assertThatThrownBy(() -> workoutService.addWorkoutsWithExercisesBulkTransactional(withNullItem))
                .isInstanceOf(IllegalArgumentException.class);

        WorkoutWithExercisesRequest requestWithNullExercises = new WorkoutWithExercisesRequest(
                "Title", "Type", 45, LocalDateTime.now().plusDays(1), 1L, 1L, null
        );

        when(workoutRepository.existsByTitleIgnoreCaseAndScheduledAt(anyString(), any())).thenReturn(false);
        when(athleteRepository.findById(1L)).thenReturn(Optional.of(new Athlete()));
        when(trainingProgramRepository.findById(1L)).thenReturn(Optional.of(new TrainingProgram("P")));

        assertThatThrownBy(() -> workoutService.addWorkoutWithExercises(requestWithNullExercises))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("exerciseNames must not be null");

        WorkoutWithExercisesRequest requestWithBlankExercises = new WorkoutWithExercisesRequest(
                "Title", "Type", 45, LocalDateTime.now().plusDays(1), 1L, 1L, List.of("  ")
        );

        assertThatThrownBy(() -> workoutService.addWorkoutWithExercises(requestWithBlankExercises))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("exerciseNames must contain at least one value");
    }
}
