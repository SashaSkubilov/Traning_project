package com.example.training_project;

import com.example.training_project.aspect.ServiceExecutionLoggingAspect;
import com.example.training_project.dto.AthleteDto;
import com.example.training_project.dto.CoachDto;
import com.example.training_project.dto.ExerciseDto;
import com.example.training_project.dto.TrainingProgramDto;
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
import com.example.training_project.config.OpenApiConfig;
import io.swagger.v3.oas.models.OpenAPI;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MapperConfigAndAspectTest {

    @Test
    void shouldMapEntitiesAndFallbackValues() {
        Coach coach = new Coach("Ilya", "Semenov");
        Athlete athlete = new Athlete("Daniil", "Kozlov", coach);
        TrainingProgram program = new TrainingProgram("Power");
        Exercise exercise = new Exercise("Row");

        Workout workout = new Workout();
        workout.setTitle("Session");
        workout.setType("Strength");
        workout.setDurationMinutes(50);
        workout.setScheduledAt(LocalDateTime.now().plusDays(1));
        workout.setAthlete(athlete);
        workout.setProgram(program);
        workout.getExercises().add(exercise);

        AthleteDto athleteDto = new AthleteMapper().toDto(athlete);
        CoachDto coachDto = new CoachMapper().toDto(coach);
        ExerciseDto exerciseDto = new ExerciseMapper().toDto(exercise);
        TrainingProgramDto trainingProgramDto = new TrainingProgramMapper().toDto(program);
        WorkoutDto workoutDto = new WorkoutMapper().toDto(workout);
        WorkoutDto workoutFallbackDto = new WorkoutMapper().toDto(new Workout());

        assertThat(athleteDto.coachName()).isEqualTo("Ilya Semenov");
        assertThat(coachDto.athletesCount()).isEqualTo(0);
        assertThat(exerciseDto.name()).isEqualTo("Row");
        assertThat(trainingProgramDto.workoutsCount()).isEqualTo(0);
        assertThat(workoutDto.exercisesCount()).isEqualTo(1);
        assertThat(workoutFallbackDto.athleteName()).isEqualTo("Unknown");
        assertThat(workoutFallbackDto.programName()).isEqualTo("No Program");
    }

    @Test
    void shouldBuildOpenApiMetadata() {
        OpenAPI openAPI = new OpenApiConfig().trainingProjectOpenApi();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Training Project API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("v1");
        assertThat(openAPI.getInfo().getContact().getName()).isEqualTo("Training Project");
    }

    @Test
    void shouldExecuteAspectAndReturnProceedResult() throws Throwable {
        ServiceExecutionLoggingAspect aspect = new ServiceExecutionLoggingAspect();
        ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
        Signature signature = Mockito.mock(Signature.class);

        Mockito.when(joinPoint.getSignature()).thenReturn(signature);
        Mockito.when(signature.toShortString()).thenReturn("WorkoutService.getAll()");
        Mockito.when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.logExecutionTime(joinPoint);

        assertThat(result).isEqualTo("ok");
        Mockito.verify(joinPoint).proceed();
    }
}
