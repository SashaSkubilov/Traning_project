package com.example.training_project.controller;

import com.example.training_project.dto.WorkoutCreateUpdateRequest;
import com.example.training_project.dto.WorkoutDto;
import com.example.training_project.service.WorkoutService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for managing workouts with transaction demonstrations.
 */
@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(final WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @GetMapping
    public List<WorkoutDto> getAll(@RequestParam(required = false) final String type) {
        return workoutService.getWorkouts(type);
    }

    @GetMapping("/optimized")
    public List<WorkoutDto> getAllOptimized() {
        return workoutService.getAllWorkoutsOptimized();
    }

    @GetMapping("/{id}")
    public WorkoutDto getById(@PathVariable final Long id) {
        return workoutService.getWorkoutById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutDto create(@RequestBody final WorkoutCreateUpdateRequest request) {
        return workoutService.createWorkout(request);
    }

    @PutMapping("/{id}")
    public WorkoutDto update(@PathVariable final Long id,
                             @RequestBody final WorkoutCreateUpdateRequest request) {
        return workoutService.updateWorkout(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final Long id) {
        workoutService.deleteWorkout(id);
    }

    @PostMapping("/demo/save-without-tx")
    public String saveWithoutTransaction() {
        try {
            workoutService.saveRelatedEntitiesWithoutTransactional();
        } catch (Exception e) {
            return "Failed; " + workoutService.getPersistedCounts();
        }
        return "Success; " + workoutService.getPersistedCounts();
    }

    @PostMapping("/demo/save-with-tx")
    public String saveWithTransaction() {
        try {
            workoutService.saveRelatedEntitiesWithTransactionalRollback();
        } catch (Exception e) {
            return "Rolled back; " + workoutService.getPersistedCounts();
        }
        return "Success; " + workoutService.getPersistedCounts();
    }
}