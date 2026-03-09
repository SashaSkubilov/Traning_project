package com.example.training_project.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for creating workout together with exercises.
 */
public record WorkoutWithExercisesRequest(
        String title,
        String type,
        Integer durationMinutes,
        LocalDateTime scheduledAt,
        Long athleteId,
        Long programId,
        List<String> exerciseNames
) {
}
