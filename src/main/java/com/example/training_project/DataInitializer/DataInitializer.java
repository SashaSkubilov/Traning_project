package com.example.training_project.DataInitializer;

import com.example.training_project.entity.Athlete;
import com.example.training_project.entity.Coach;
import com.example.training_project.entity.Exercise;
import com.example.training_project.entity.TrainingProgram;
import com.example.training_project.entity.Workout;
import com.example.training_project.repository.AthleteRepository;
import com.example.training_project.repository.CoachRepository;
import com.example.training_project.repository.ExerciseRepository;
import com.example.training_project.repository.TrainingProgramRepository;
import com.example.training_project.repository.WorkoutRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            CoachRepository coachRepository,
            AthleteRepository athleteRepository,
            TrainingProgramRepository trainingProgramRepository,
            ExerciseRepository exerciseRepository,
            WorkoutRepository workoutRepository
    ) {
        return args -> {
            Coach coach = coachRepository.save(new Coach("Ivan Petrov"));
            Athlete athlete = athleteRepository.save(new Athlete("Alex Smirnov", coach));
            TrainingProgram program = trainingProgramRepository.save(new TrainingProgram("Mass Gain"));
            Exercise squat = exerciseRepository.save(new Exercise("Squat"));
            Exercise bench = exerciseRepository.save(new Exercise("Bench Press"));
            Workout first = new Workout("Leg Day", 70, LocalDateTime.now().plusDays(1), athlete, program);
            first.getExercises().addAll(List.of(squat, bench));
            workoutRepository.save(first);
            Workout second = new Workout("Upper Day", 60, LocalDateTime.now().plusDays(2), athlete, program);
            second.getExercises().add(bench);
            workoutRepository.save(second);
        };
    }
}
