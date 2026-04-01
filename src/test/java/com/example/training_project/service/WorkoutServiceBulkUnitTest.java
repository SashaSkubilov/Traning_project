package com.example.training_project.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceBulkUnitTest {

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
    @Mock
    private WorkoutMapper workoutMapper;

    @InjectMocks
    private WorkoutService workoutService;

    @BeforeEach
    void setUp() {
        when(workoutRepository.existsByTitleIgnoreCaseAndScheduledAt(anyString(), any())).thenReturn(false);
        when(athleteRepository.findById(any())).thenReturn(Optional.of(new Athlete()));
        when(trainingProgramRepository.findById(any())).thenReturn(Optional.of(new TrainingProgram("Program")));

        when(exerciseRepository.save(any(Exercise.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workoutRepository.save(any(Workout.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workoutMapper.toDto(any(Workout.class))).thenAnswer(invocation -> {
            Workout workout = invocation.getArgument(0);
            return new WorkoutDto(
                    1L,
                    workout.getTitle(),
                    workout.getType(),
                    workout.getDurationMinutes(),
                    workout.getScheduledAt(),
                    "Athlete",
                    "Program",
                    workout.getExercises().size()
            );
        });
    }

    @Test
    void shouldCreateAllWorkoutsInTransactionalBulk() {
        List<WorkoutWithExercisesRequest> requests = List.of(
                request(" Upper A ", " Strength ", List.of(" Squat ")),
                request(" Upper B ", " Cardio ", List.of(" Row "))
        );

        List<WorkoutDto> result = workoutService.addWorkoutsWithExercisesBulkTransactional(requests);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(WorkoutDto::title).containsExactly("Upper A", "Upper B");
        verify(workoutRepository, times(2)).save(any(Workout.class));
    }

    @Test
    void shouldRejectNullBulkPayload() {
        assertThatThrownBy(() -> workoutService.addWorkoutsWithExercisesBulkTransactional(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bulk request must contain at least one workout");
    }

    @Test
    void shouldDemonstrateNonTransactionalBehaviorOnFailure() {
        List<WorkoutWithExercisesRequest> requests = List.of(
                request("Bulk ok", "Strength", List.of("Deadlift")),
                request("FAIL", "Strength", List.of("Bench Press"))
        );

        assertThatThrownBy(() -> workoutService.addWorkoutsWithExercisesBulkNonTransactional(requests))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Simulated failure after saving exercises");

        verify(workoutRepository, times(1)).save(any(Workout.class));
        verify(exerciseRepository, times(2)).save(any(Exercise.class));
    }

    @Test
    void shouldNormalizeExerciseNamesInBulkRequest() {
        List<WorkoutWithExercisesRequest> requests = List.of(
                request("Bulk normalized", "Strength", List.of("  Pull Up  ", " ", "  Push Up  "))
        );

        workoutService.addWorkoutsWithExercisesBulkTransactional(requests);

        ArgumentCaptor<Exercise> captor = ArgumentCaptor.forClass(Exercise.class);
        verify(exerciseRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(Exercise::getName)
                .containsExactly("Pull Up", "Push Up");
    }

    private WorkoutWithExercisesRequest request(final String title,
                                                final String type,
                                                final List<String> exerciseNames) {
        return new WorkoutWithExercisesRequest(
                title,
                type,
                45,
                LocalDateTime.now().plusDays(2),
                1L,
                1L,
                exerciseNames
        );
    }
}
