package com.example.training_project.controller;

import com.example.training_project.dto.WorkoutDto;
import com.example.training_project.service.WorkoutService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {
    private final WorkoutService service;

    public WorkoutController(WorkoutService service) {
        this.service = service;
    }

    // Пример: http://localhost:8080/api/workouts?type=Кардио
    @GetMapping
    public List<WorkoutDto> getAll(@RequestParam(required = false) String type) {
        return service.getWorkouts(type);
    }

    // Пример: http://localhost:8080/api/workouts/1
    @GetMapping("/{id}")
    public WorkoutDto getById(@PathVariable Long id) {
        return service.getWorkoutById(id);
    }
}