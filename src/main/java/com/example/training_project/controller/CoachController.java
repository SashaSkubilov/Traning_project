package com.example.training_project.controller;

import com.example.training_project.dto.CoachCreateUpdateRequest;
import com.example.training_project.dto.CoachDto;
import com.example.training_project.service.CoachService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/coaches")
@Tag(name = "Coaches", description = "CRUD операции для тренеров")
public class CoachController {

    private final CoachService coachService;

    public CoachController(final CoachService coachService) {
        this.coachService = coachService;
    }

    @GetMapping
    @Operation(summary = "Получить всех тренеров")
    public List<CoachDto> getAll() {
        return coachService.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить тренера по id")
    public CoachDto getById(@PathVariable final Long id) {
        return coachService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать тренера")
    public CoachDto create(@Valid @RequestBody final CoachCreateUpdateRequest request) {
        return coachService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить тренера")
    public CoachDto update(@PathVariable final Long id,
                           @Valid @RequestBody final CoachCreateUpdateRequest request) {
        return coachService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить тренера")
    public void delete(@PathVariable final Long id) {
        coachService.delete(id);
    }
}
