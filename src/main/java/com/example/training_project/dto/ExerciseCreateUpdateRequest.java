package com.example.training_project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на создание или обновление упражнения")
public record ExerciseCreateUpdateRequest(
        @Schema(description = "Название упражнения", example = "Bench Press")
        @NotBlank(message = "name must not be blank")
        @Size(max = 150, message = "name must be at most 150 characters")
        String name
) {
}
