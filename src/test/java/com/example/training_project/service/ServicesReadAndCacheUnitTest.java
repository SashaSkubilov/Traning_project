package com.example.training_project.service;

import com.example.training_project.dto.WorkoutDto;
import com.example.training_project.entity.Athlete;
import com.example.training_project.entity.Coach;
import com.example.training_project.entity.Exercise;
import com.example.training_project.entity.TrainingProgram;
import com.example.training_project.entity.Workout;
import com.example.training_project.mapper.AthleteMapper;
import com.example.training_project.mapper.CoachMapper;
import com.example.training_project.mapper.ExerciseMapper;
import com.example.training_project.mapper.TrainingProgramMapper;
import com.example.training_project.mapper.WorkoutMapper;
import com.example.training_project.repository.AthleteRepository;
import com.example.training_project.repository.CoachRepository;
import com.example.training_project.repository.ExerciseRepository;
import com.example.training_project.repository.TrainingProgramRepository;
import com.example.training_project.repository.WorkoutRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicesReadAndCacheUnitTest {

    @Mock
    private AthleteRepository athleteRepository;
    @Mock
    private CoachRepository coachRepository;
    @Mock
    private ExerciseRepository exerciseRepository;
    @Mock
    private TrainingProgramRepository trainingProgramRepository;
    @Mock
    private WorkoutRepository workoutRepository;

    @Test
    void shouldCoverReadPathsForCrudServices() {
        AthleteService athleteService = new AthleteService(athleteRepository, coachRepository, new AthleteMapper());
        CoachService coachService = new CoachService(coachRepository, new CoachMapper());
        ExerciseService exerciseService = new ExerciseService(exerciseRepository, new ExerciseMapper());
        TrainingProgramService programService = new TrainingProgramService(
                trainingProgramRepository,
                new TrainingProgramMapper()
        );

        Coach coach = new Coach("Ivan", "Petrov");
        Athlete athlete = new Athlete("Alex", "Smirnov", coach);
        Exercise exercise = new Exercise("Row");
        TrainingProgram program = new TrainingProgram("Mass");

        when(athleteRepository.findAll()).thenReturn(List.of(athlete));
        when(athleteRepository.findById(1L)).thenReturn(Optional.of(athlete));
        when(coachRepository.findAll()).thenReturn(List.of(coach));
        when(coachRepository.findById(1L)).thenReturn(Optional.of(coach));
        when(exerciseRepository.findAll()).thenReturn(List.of(exercise));
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(trainingProgramRepository.findAll()).thenReturn(List.of(program));
        when(trainingProgramRepository.findById(1L)).thenReturn(Optional.of(program));

        assertThat(athleteService.getAll()).hasSize(1);
        assertThat(athleteService.getById(1L).coachName()).isEqualTo("Ivan Petrov");
        assertThat(coachService.getAll()).hasSize(1);
        assertThat(coachService.getById(1L).lastName()).isEqualTo("Petrov");
        assertThat(exerciseService.getAll()).hasSize(1);
        assertThat(exerciseService.getById(1L).name()).isEqualTo("Row");
        assertThat(programService.getAll()).hasSize(1);
        assertThat(programService.getById(1L).name()).isEqualTo("Mass");
    }

    @Test
    void shouldCoverWorkoutReadAndCacheHitPaths() {
        WorkoutService workoutService = new WorkoutService(
                workoutRepository,
                athleteRepository,
                trainingProgramRepository,
                exerciseRepository,
                coachRepository,
                new WorkoutMapper()
        );

        Workout workout = new Workout();
        workout.setTitle("Leg Day");
        workout.setType("Strength");

        when(workoutRepository.findAll()).thenReturn(List.of(workout));
        when(workoutRepository.findAllWithDetails()).thenReturn(List.of(workout));
        when(workoutRepository.findById(1L)).thenReturn(Optional.of(workout));
        when(workoutRepository.findByFiltersNative(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(new WorkoutDto(
                        1L,
                        "Leg Day",
                        "Strength",
                        45,
                        java.time.LocalDateTime.now(),
                        "Athlete",
                        "Program",
                        1
                ))));

        assertThat(workoutService.getAllWorkouts()).hasSize(1);
        assertThat(workoutService.getAllWorkoutsOptimized()).hasSize(1);
        assertThat(workoutService.getWorkoutById(1L).title()).isEqualTo("Leg Day");
        assertThat(workoutService.getWorkouts(null)).hasSize(1);
        assertThat(workoutService.getWorkouts("Cardio")).isEmpty();

        PageRequest pageable = PageRequest.of(0, 10);
        workoutService.searchWorkoutsNative("Coach", "Program", pageable);
        workoutService.searchWorkoutsNative("Coach", "Program", pageable);

        verify(workoutRepository, times(1)).findByFiltersNative("Coach", "Program", pageable);
    }
}
