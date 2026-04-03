package com.example.training_project.service;

import com.example.training_project.dto.WorkoutCreateUpdateRequest;
import com.example.training_project.dto.WorkoutWithExercisesRequest;
import com.example.training_project.entity.Athlete;
import com.example.training_project.entity.Exercise;
import com.example.training_project.entity.TrainingProgram;
import com.example.training_project.entity.Workout;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceCompletionUnitTest {

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
    void shouldCacheJpqlResultsForBlankType() {
        Workout workout = new Workout();
        workout.setTitle("Leg Day");
        workout.setType("Strength");
        PageRequest pageable = PageRequest.of(0, 5);

        when(workoutRepository.findByFiltersJpql(anyString(), anyString(), anyString(), any()))
                .thenReturn(new PageImpl<>(List.of(workout)));

        assertThat(workoutService.searchWorkoutsJpql(" ", "Coach", "Program", pageable).getContent())
                .hasSize(1);
        assertThat(workoutService.searchWorkoutsJpql(" ", "Coach", "Program", pageable).getContent())
                .hasSize(1);

        verify(workoutRepository, times(1)).findByFiltersJpql(" ", "Coach", "Program", pageable);
    }

    @Test
    void shouldCreateAndUpdateWorkoutSuccessfully() {
        WorkoutCreateUpdateRequest request = new WorkoutCreateUpdateRequest(
                " Title ",
                " Type ",
                45,
                LocalDateTime.now().plusDays(2),
                1L,
                2L,
                List.of(10L)
        );

        Athlete athlete = new Athlete();
        TrainingProgram program = new TrainingProgram("Mass");
        Exercise exercise = new Exercise("Row");

        when(workoutRepository.existsByTitleIgnoreCaseAndScheduledAt(anyString(), any())).thenReturn(false);
        when(workoutRepository.existsByTitleIgnoreCaseAndScheduledAtAndIdNot(anyString(), any(), anyLong()))
                .thenReturn(false);
        when(athleteRepository.findById(1L)).thenReturn(Optional.of(athlete));
        when(trainingProgramRepository.findById(2L)).thenReturn(Optional.of(program));
        when(exerciseRepository.findAllById(List.of(10L))).thenReturn(List.of(exercise));
        when(workoutRepository.save(any(Workout.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workoutRepository.findById(5L)).thenReturn(Optional.of(new Workout()));

        assertThat(workoutService.createWorkout(request).title()).isEqualTo("Title");
        assertThat(workoutService.updateWorkout(5L, request).type()).isEqualTo("Type");
    }

    @Test
    void shouldUseInMemoryTypeFilteringWithPaginationWindow() {
        Workout strength = new Workout();
        strength.setTitle("A");
        strength.setType("Strength");
        Workout cardio = new Workout();
        cardio.setTitle("B");
        cardio.setType("Cardio");

        when(workoutRepository.findByFiltersJpql(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(strength, cardio)));

        PageRequest pageable = PageRequest.of(0, 1);
        assertThat(workoutService.searchWorkoutsJpql("strength", "Coach", "Program", pageable).getContent())
                .hasSize(1);
        verify(workoutRepository).findByFiltersJpql(null, "Coach", "Program", PageRequest.of(0, Integer.MAX_VALUE));
    }

    @Test
    void shouldThrowOnUpdateWhenWorkoutMissing() {
        WorkoutCreateUpdateRequest request = new WorkoutCreateUpdateRequest(
                "Title",
                "Type",
                45,
                LocalDateTime.now().plusDays(2),
                1L,
                2L,
                List.of(10L)
        );

        when(workoutRepository.existsByTitleIgnoreCaseAndScheduledAtAndIdNot(anyString(), any(), anyLong()))
                .thenReturn(false);
        when(workoutRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.updateWorkout(999L, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldFailForNullNonTransactionalBulkInput() {
        assertThatThrownBy(() -> workoutService.addWorkoutsWithExercisesBulkNonTransactional(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bulk request must contain at least one workout");
    }

    @Test
    void shouldFailForNullWorkoutPayloadInNonTransactionalBulkInput() {
        List<WorkoutWithExercisesRequest> requests = new ArrayList<>();
        requests.add(null);
        assertThatThrownBy(() -> workoutService.addWorkoutsWithExercisesBulkNonTransactional(requests))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bulk request must not contain null workout payloads");
    }
}
