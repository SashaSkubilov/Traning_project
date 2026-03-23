package com.example.training_project.controller;

import com.example.training_project.dto.TrainingProgramCreateUpdateRequest;
import com.example.training_project.dto.TrainingProgramDto;
import com.example.training_project.service.TrainingProgramService;
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
@RequestMapping("/api/programs")
@Tag(name = "Programs", description = "CRUD операции для тренировочных программ")
public class TrainingProgramController {

    private final TrainingProgramService trainingProgramService;

    public TrainingProgramController(final TrainingProgramService trainingProgramService) {
        this.trainingProgramService = trainingProgramService;
    }

    @GetMapping
    @Operation(summary = "Получить все программы")
    public List<TrainingProgramDto> getAll() {
        return trainingProgramService.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить программу по id")
    public TrainingProgramDto getById(@PathVariable final Long id) {
        return trainingProgramService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать программу")
    public TrainingProgramDto create(@Valid @RequestBody final TrainingProgramCreateUpdateRequest request) {
        return trainingProgramService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить программу")
    public TrainingProgramDto update(@PathVariable final Long id,
                                     @Valid @RequestBody final TrainingProgramCreateUpdateRequest request) {
        return trainingProgramService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить программу")
    public void delete(@PathVariable final Long id) {
        trainingProgramService.delete(id);
    }
}
