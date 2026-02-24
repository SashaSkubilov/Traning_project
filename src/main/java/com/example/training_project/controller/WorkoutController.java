package com.example.training_project.controller;

import com.example.training_project.dto.WorkoutDto;
import com.example.training_project.service.WorkoutService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService service;

    public WorkoutController(WorkoutService service) {
        this.service = service;
    }

    @GetMapping
    public List<WorkoutDto> getAll(@RequestParam(required = false) String type) {
        return service.getWorkouts(type);
    }

    @GetMapping("/{id}")
    public WorkoutDto getById(@PathVariable Long id) {
        return service.getWorkoutById(id);
    }
}
