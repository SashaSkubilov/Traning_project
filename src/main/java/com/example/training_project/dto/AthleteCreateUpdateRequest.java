package com.example.training_project.dto;

public record AthleteCreateUpdateRequest(
        String firstName,
        String lastName,
        Long coachId
) {
}
