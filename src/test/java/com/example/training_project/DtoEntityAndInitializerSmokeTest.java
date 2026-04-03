package com.example.training_project;

import com.example.training_project.dto.AthleteCreateUpdateRequest;
import com.example.training_project.dto.AthleteDto;
import com.example.training_project.dto.CoachCreateUpdateRequest;
import com.example.training_project.dto.CoachDto;
import com.example.training_project.dto.ExerciseCreateUpdateRequest;
import com.example.training_project.dto.ExerciseDto;
import com.example.training_project.dto.TrainingProgramCreateUpdateRequest;
import com.example.training_project.dto.TrainingProgramDto;
import com.example.training_project.dto.WorkoutCreateUpdateRequest;
import com.example.training_project.dto.WorkoutDto;
import com.example.training_project.dto.WorkoutWithExercisesRequest;
import com.example.training_project.dto.error.ApiErrorResponse;
import com.example.training_project.entity.Athlete;
import com.example.training_project.entity.Coach;
import com.example.training_project.entity.Exercise;
import com.example.training_project.entity.TrainingProgram;
import com.example.training_project.entity.Workout;
import com.example.training_project.exception.DuplicateResourceException;
import com.example.training_project.repository.AthleteRepository;
import com.example.training_project.repository.CoachRepository;
import com.example.training_project.repository.ExerciseRepository;
import com.example.training_project.repository.TrainingProgramRepository;
import com.example.training_project.repository.WorkoutRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.CommandLineRunner;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DtoEntityAndInitializerSmokeTest {

    @Test
    void shouldInstantiateAllDtosAndDomainObjects() {
        LocalDateTime scheduledAt = LocalDateTime.now().plusDays(1);
        AthleteCreateUpdateRequest athleteRequest = new AthleteCreateUpdateRequest("A", "B", 1L);
        CoachCreateUpdateRequest coachRequest = new CoachCreateUpdateRequest("C", "D");
        ExerciseCreateUpdateRequest exerciseRequest = new ExerciseCreateUpdateRequest("Push Up");
        TrainingProgramCreateUpdateRequest programRequest = new TrainingProgramCreateUpdateRequest("Mass");
        WorkoutCreateUpdateRequest workoutRequest = new WorkoutCreateUpdateRequest(
                "Leg Day", "Strength", 60, scheduledAt, 1L, 1L, List.of(1L)
        );
        WorkoutWithExercisesRequest withExercisesRequest = new WorkoutWithExercisesRequest(
                "Upper", "Hypertrophy", 45, scheduledAt, 1L, 1L, List.of("Fly")
        );

        AthleteDto athleteDto = new AthleteDto(1L, "A", "B", 2L, "Coach Name");
        CoachDto coachDto = new CoachDto(2L, "C", "D", 1);
        ExerciseDto exerciseDto = new ExerciseDto(3L, "Push Up");
        TrainingProgramDto trainingProgramDto = new TrainingProgramDto(4L, "Mass", 2);
        WorkoutDto workoutDto = new WorkoutDto(5L, "Leg Day", "Strength", 60, scheduledAt,
                "A B", "Mass", 1);
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC), 400, "Bad Request", "Validation failed", "/api", List.of("x")
        );

        Coach coach = new Coach("Name", "Surname");
        Athlete athlete = new Athlete("Ath", "One", coach);
        Exercise exercise = new Exercise("Sit Up");
        TrainingProgram program = new TrainingProgram("Plan");
        Workout workout = new Workout("Title", 30, scheduledAt, athlete, program);
        workout.setType("Cardio");
        workout.getExercises().add(exercise);

        DuplicateResourceException duplicateResourceException = new DuplicateResourceException("duplicate");

        assertThat(athleteRequest.firstName()).isEqualTo("A");
        assertThat(coachRequest.lastName()).isEqualTo("D");
        assertThat(exerciseRequest.name()).isEqualTo("Push Up");
        assertThat(programRequest.name()).isEqualTo("Mass");
        assertThat(workoutRequest.exerciseIds()).containsExactly(1L);
        assertThat(withExercisesRequest.exerciseNames()).containsExactly("Fly");
        assertThat(athleteDto.coachName()).isEqualTo("Coach Name");
        assertThat(coachDto.athletesCount()).isEqualTo(1);
        assertThat(exerciseDto.name()).isEqualTo("Push Up");
        assertThat(trainingProgramDto.workoutsCount()).isEqualTo(2);
        assertThat(workoutDto.programName()).isEqualTo("Mass");
        assertThat(apiErrorResponse.error()).isEqualTo("Bad Request");
        assertThat(workout.getExercises()).hasSize(1);
        assertThat(duplicateResourceException).hasMessage("duplicate");
    }

    @Test
    void shouldInitializeDataWhenRepositoryIsEmptyAndSkipWhenNotEmpty() throws Exception {
        CoachRepository coachRepository = Mockito.mock(CoachRepository.class);
        AthleteRepository athleteRepository = Mockito.mock(AthleteRepository.class);
        TrainingProgramRepository programRepository = Mockito.mock(TrainingProgramRepository.class);
        ExerciseRepository exerciseRepository = Mockito.mock(ExerciseRepository.class);
        WorkoutRepository workoutRepository = Mockito.mock(WorkoutRepository.class);

        when(workoutRepository.count()).thenReturn(1L);
        CommandLineRunner skipRunner = new DataInitializer().initData(
                coachRepository, athleteRepository, programRepository, exerciseRepository, workoutRepository
        );
        skipRunner.run();
        verify(workoutRepository, never()).save(any(Workout.class));

        when(workoutRepository.count()).thenReturn(0L);
        when(coachRepository.save(any(Coach.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(athleteRepository.save(any(Athlete.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(programRepository.save(any(TrainingProgram.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(exerciseRepository.save(any(Exercise.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workoutRepository.save(any(Workout.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommandLineRunner initRunner = new DataInitializer().initData(
                coachRepository, athleteRepository, programRepository, exerciseRepository, workoutRepository
        );
        initRunner.run();

        verify(workoutRepository, Mockito.times(2)).save(any(Workout.class));
    }
}
