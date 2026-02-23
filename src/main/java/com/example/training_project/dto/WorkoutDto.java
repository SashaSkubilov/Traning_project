package com.example.training_project.dto;

public record WorkoutDto(
        Long id,
        String type,
        Integer durationMinutes,
        String formattedDate
) { }