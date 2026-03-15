package com.example.training_project.controller;

import com.example.training_project.dto.ExerciseCreateUpdateRequest;
import com.example.training_project.dto.ExerciseDto;
import com.example.training_project.service.ExerciseService;
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
@RequestMapping("/api/exercises")
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(final ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @GetMapping
    public List<ExerciseDto> getAll() {
        return exerciseService.getAll();
    }

    @GetMapping("/{id}")
    public ExerciseDto getById(@PathVariable final Long id) {
        return exerciseService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExerciseDto create(@RequestBody final ExerciseCreateUpdateRequest request) {
        return exerciseService.create(request);
    }

    @PutMapping("/{id}")
    public ExerciseDto update(@PathVariable final Long id,
                              @RequestBody final ExerciseCreateUpdateRequest request) {
        return exerciseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final Long id) {
        exerciseService.delete(id);
    }
}
