package com.example.training_project.dto;

public record AthleteDto(
        Long id,
        String firstName,
        String lastName,
        Long coachId,
        String coachName
) {
}

