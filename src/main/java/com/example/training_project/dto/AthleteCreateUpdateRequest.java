package com.example.training_project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на создание или обновление спортсмена")
public record AthleteCreateUpdateRequest(
        @Schema(description = "Имя спортсмена", example = "Alex")
        @NotBlank(message = "firstName must not be blank")
        @Size(max = 100, message = "firstName must be at most 100 characters")
        String firstName,

        @Schema(description = "Фамилия спортсмена", example = "Smirnov")
        @NotBlank(message = "lastName must not be blank")
        @Size(max = 100, message = "lastName must be at most 100 characters")
        String lastName,

        @Schema(description = "Идентификатор тренера", example = "1")
        Long coachId
) {
}
