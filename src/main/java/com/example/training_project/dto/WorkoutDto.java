package com.example.training_project.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Workout.
 */
public record WorkoutDto(
        Long id,
        String title,
        String type,
        Integer durationMinutes,
        LocalDateTime scheduledAt,
        String athleteName,
        String programName,
        int exercisesCount
) {
}
