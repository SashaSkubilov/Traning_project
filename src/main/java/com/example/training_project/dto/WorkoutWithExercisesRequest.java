package com.example.training_project.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Запрос на создание тренировки вместе с новыми упражнениями")
public record WorkoutWithExercisesRequest(
        @Schema(description = "Название тренировки", example = "Upper Day")
        @NotBlank(message = "title must not be blank")
        @Size(max = 150, message = "title must be at most 150 characters")
        String title,

        @Schema(description = "Тип тренировки", example = "Hypertrophy")
        @NotBlank(message = "type must not be blank")
        @Size(max = 100, message = "type must be at most 100 characters")
        String type,

        @Schema(description = "Длительность в минутах", example = "60")
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

        @ArraySchema(schema = @Schema(description = "Название упражнения", example = "Pull Up"),
                arraySchema = @Schema(description = "Список названий новых упражнений"))
        @NotEmpty(message = "exerciseNames must not be empty")
        List<@NotBlank(message = "exerciseNames must not contain blank values") String> exerciseNames
) {
}
