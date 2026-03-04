package com.example.training_project.dto;

import java.time.LocalDateTime;

/**
 * Request DTO for creating or updating a Workout.
 */
public record WorkoutCreateUpdateRequest(
        String title,
        String type,
        Integer durationMinutes,
        LocalDateTime scheduledAt,
        Long athleteId,
        Long programId,
        java.util.List<Long> exerciseIds
) {
}
