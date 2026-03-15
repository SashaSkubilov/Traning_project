package com.example.training_project.controller;

import com.example.training_project.dto.WorkoutCreateUpdateRequest;
import com.example.training_project.dto.WorkoutDto;
import com.example.training_project.dto.WorkoutWithExercisesRequest;
import com.example.training_project.service.WorkoutService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/search/jpql")
    public Page<WorkoutDto> searchJpql(
            @RequestParam(required = false) final String type,
            @RequestParam(required = false) final Long coachId,
            @RequestParam(required = false) final Long programId,
            final Pageable pageable
    ) {
        return workoutService.searchWorkoutsJpql(type, coachId, programId, pageable);
    }

    @GetMapping("/search/native")
    public Page<WorkoutDto> searchNative(
            @RequestParam(required = false) final String type,
            @RequestParam(required = false) final Long coachId,
            @RequestParam(required = false) final Long programId,
            final Pageable pageable
    ) {
        return workoutService.searchWorkoutsNative(type, coachId, programId, pageable);
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

    @GetMapping("/persisted_counts")
    public String getPersistedCounts() {
        return workoutService.getPersistedCounts();
    }

    @PostMapping("/with_exercises")
    public ResponseEntity<WorkoutDto> addWorkoutWithExercises(
            @RequestBody final WorkoutWithExercisesRequest request
    ) {
        WorkoutDto created = workoutService.addWorkoutWithExercises(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/with_exercises_without_tx")
    public ResponseEntity<WorkoutDto> addWorkoutWithExercisesWithoutTransaction(
            @RequestBody final WorkoutWithExercisesRequest request
    ) {
        WorkoutDto created = workoutService.addWorkoutWithExercisesWithoutTransaction(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}
