package com.example.training_project.dto.error;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Единый формат ошибки API")
public record ApiErrorResponse(
        @Schema(description = "Время возникновения ошибки", example = "2026-03-23T12:00:00Z")
        OffsetDateTime timestamp,

        @Schema(description = "HTTP статус", example = "400")
        int status,

        @Schema(description = "Краткое название ошибки", example = "Bad Request")
        String error,

        @Schema(description = "Сообщение об ошибке", example = "Validation failed")
        String message,

        @Schema(description = "Путь запроса", example = "/api/workouts")
        String path,

        @ArraySchema(schema = @Schema(description = "Деталь ошибки", example = "title: must not be blank"),
                arraySchema = @Schema(description = "Список деталей ошибки"))
        List<String> details
) {
}
