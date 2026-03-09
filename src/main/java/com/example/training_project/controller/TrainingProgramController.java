package com.example.training_project.controller;

import com.example.training_project.dto.TrainingProgramDto;
import com.example.training_project.entity.TrainingProgram;
import com.example.training_project.service.TrainingProgramService;
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
public class TrainingProgramController {

    private final TrainingProgramService trainingProgramService;

    public TrainingProgramController(final TrainingProgramService trainingProgramService) {
        this.trainingProgramService = trainingProgramService;
    }

    @GetMapping
    public List<TrainingProgramDto> getAll() {
        return trainingProgramService.getAll();
    }

    @GetMapping("/{id}")
    public TrainingProgramDto getById(@PathVariable final Long id) {
        return trainingProgramService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrainingProgramDto create(@RequestBody final TrainingProgram program) {
        return trainingProgramService.create(program);
    }

    @PutMapping("/{id}")
    public TrainingProgramDto update(@PathVariable final Long id, @RequestBody final TrainingProgram program) {
        return trainingProgramService.update(id, program);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final Long id) {
        trainingProgramService.delete(id);
    }
}

