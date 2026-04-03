package com.example.training_project.service;

import com.example.training_project.dto.WorkoutCreateUpdateRequest;
import com.example.training_project.dto.WorkoutDto;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkoutServiceEdgeCaseUnitTest {

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
    void shouldUseBlankTypeBranchForJpqlSearch() {
        Workout workout = new Workout();
        workout.setTitle("Session");
        workout.setType("Strength");
        when(workoutRepository.findByFiltersJpql(any(), anyString(), anyString(), any()))
                .thenReturn(new PageImpl<>(List.of(workout)));

        var result = workoutService.searchWorkoutsJpql(
                "   ", "Coach", "Program", PageRequest.of(0, 5)
        );
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Session");

        verify(workoutRepository).findByFiltersJpql("   ", "Coach", "Program", PageRequest.of(0, 5));
    }

    @Test
    void shouldThrowWhenAthleteOrProgramMissingDuringCreate() {
        WorkoutCreateUpdateRequest request = new WorkoutCreateUpdateRequest(
                "Title", "Type", 45, LocalDateTime.now().plusDays(1), 1L, 2L, List.of(1L)
        );

        when(workoutRepository.existsByTitleIgnoreCaseAndScheduledAt(anyString(), any())).thenReturn(false);
        when(athleteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.createWorkout(request))
                .isInstanceOf(EntityNotFoundException.class);

        when(athleteRepository.findById(1L)).thenReturn(Optional.of(new Athlete()));
        when(trainingProgramRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutService.createWorkout(request)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldFailOnSpecialFailTitleAndSupportNonTransactionalBulk() {
        LocalDateTime scheduledAt = LocalDateTime.now().plusDays(2);
        WorkoutWithExercisesRequest failRequest = new WorkoutWithExercisesRequest(
                "FAIL", "Type", 30, scheduledAt, 1L, 1L, List.of(" Row ")
        );

        when(workoutRepository.existsByTitleIgnoreCaseAndScheduledAt(anyString(), any())).thenReturn(false);
        when(athleteRepository.findById(1L)).thenReturn(Optional.of(new Athlete()));
        when(trainingProgramRepository.findById(1L)).thenReturn(Optional.of(new TrainingProgram("Mass")));
        when(exerciseRepository.save(any(Exercise.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> workoutService.addWorkoutWithExercises(failRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Simulated failure after saving exercises");

        WorkoutWithExercisesRequest okRequest = new WorkoutWithExercisesRequest(
                "Title", "Type", 30, scheduledAt.plusHours(1), 1L, 1L, List.of("Row")
        );
        Workout persisted = new Workout();
        persisted.setTitle("Title");
        persisted.setType("Type");
        when(workoutRepository.save(any(Workout.class))).thenReturn(persisted);

        List<WorkoutDto> created = workoutService.addWorkoutsWithExercisesBulkNonTransactional(
                List.of(okRequest)
        );

        assertThat(created).hasSize(1);
    }

    @Test
    void shouldDeleteAndBuildPersistedCountsString() {
        when(coachRepository.count()).thenReturn(2L);
        when(athleteRepository.count()).thenReturn(3L);
        when(trainingProgramRepository.count()).thenReturn(4L);
        when(exerciseRepository.count()).thenReturn(5L);
        when(workoutRepository.count()).thenReturn(6L);

        assertThat(workoutService.getPersistedCounts())
                .isEqualTo("coaches=2, athletes=3, programs=4, exercises=5, workouts=6");

        workoutService.deleteWorkout(7L);

        verify(workoutRepository).deleteById(7L);
    }

    @Test
    void shouldSupportPaginationBeyondFilteredSize() {
        Workout strength = new Workout();
        strength.setTitle("Only");
        strength.setType("Strength");

        when(workoutRepository.findByFiltersJpql(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(strength)));

        assertThat(workoutService.searchWorkoutsJpql(
                "strength", null, null, PageRequest.of(3, 10)
        ).getContent())
                .isEmpty();
    }

    @Test
    void shouldCreateWithTrimmedExerciseNamesAfterBulkNormalization() {
        List<String> exerciseNames = new ArrayList<>();
        exerciseNames.add("  Pull up  ");
        exerciseNames.add(null);
        exerciseNames.add(" ");

        WorkoutWithExercisesRequest request = new WorkoutWithExercisesRequest(
                "  Title  ",
                " Type ",
                40,
                LocalDateTime.now().plusDays(2),
                1L,
                1L,
                exerciseNames
        );

        when(workoutRepository.existsByTitleIgnoreCaseAndScheduledAt(anyString(), any())).thenReturn(false);
        when(athleteRepository.findById(anyLong())).thenReturn(Optional.of(new Athlete()));
        when(trainingProgramRepository.findById(anyLong()))
                .thenReturn(Optional.of(new TrainingProgram("Mass")));
        when(exerciseRepository.save(any(Exercise.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workoutRepository.save(any(Workout.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<WorkoutDto> created = workoutService.addWorkoutsWithExercisesBulkTransactional(List.of(request));

        assertThat(created).hasSize(1);
        verify(exerciseRepository).save(any(Exercise.class));
    }
}
