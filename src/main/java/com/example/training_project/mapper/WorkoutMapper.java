package com.example.training_project.mapper;

import com.example.training_project.dto.WorkoutDto;
import com.example.training_project.entity.Workout;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting Workout entity to DTO.
 */
@Component
public class WorkoutMapper {

    public WorkoutDto toDto(final Workout workout) {
        return new WorkoutDto(
                workout.getId(),
                workout.getTitle(),
                workout.getType(),
                workout.getDurationMinutes(),
                workout.getScheduledAt(),
                workout.getAthlete() != null ? workout.getAthlete().getFullName() : "Unknown",
                workout.getProgram() != null ? workout.getProgram().getName() : "No Program",
                workout.getExercises() != null ? workout.getExercises().size() : 0
        );
    }
}