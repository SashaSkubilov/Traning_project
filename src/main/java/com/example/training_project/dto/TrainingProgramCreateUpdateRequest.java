package com.example.training_project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на создание или обновление тренировочной программы")
public record TrainingProgramCreateUpdateRequest(
        @Schema(description = "Название программы", example = "Mass Gain")
        @NotBlank(message = "name must not be blank")
        @Size(max = 150, message = "name must be at most 150 characters")
        String name
) {
}
