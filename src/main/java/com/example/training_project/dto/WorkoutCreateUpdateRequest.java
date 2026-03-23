package com.example.training_project.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Запрос на создание или обновление тренировки")
public record WorkoutCreateUpdateRequest(
        @Schema(description = "Название тренировки", example = "Leg Day")
        @NotNull(message = "title must not be null")
        @Size(min = 2, max = 150, message = "title length must be between 2 and 150 characters")
        String title,

        @Schema(description = "Название тренировки", example = "Leg Day")
        @NotNull(message = "title must not be null")
        @Size(min = 2, max = 150, message = "title length must be between 2 and 150 characters")
        String type,

        @Schema(description = "Длительность в минутах", example = "70")
        @NotNull(message = "durationMinutes must not be null")
        @Min(value = 1, message = "durationMinutes must be greater than 0")
        Integer durationMinutes,

        @Schema(description = "Дата и время тренировки", example = "2026-03-30T10:00:00")
        @NotNull(message = "scheduledAt must not be null")
        @Future(message = "scheduledAt must be in the future")
        LocalDateTime scheduledAt,

        @Schema(description = "Идентификатор спортсмена", example = "1")
        @NotNull(message = "athleteId must not be null")
        Long athleteId,

        @Schema(description = "Идентификатор программы", example = "1")
        @NotNull(message = "programId must not be null")
        Long programId,

        @ArraySchema(schema = @Schema(description = "Идентификатор упражнения", example = "1"),
                arraySchema = @Schema(description = "Список идентификаторов упражнений"))
        @NotEmpty(message = "exerciseIds must not be empty")
        List<@NotNull(message = "exerciseIds must not contain null values") Long> exerciseIds
) {
}
