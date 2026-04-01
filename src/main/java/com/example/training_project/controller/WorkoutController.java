package com.example.training_project.controller;

import com.example.training_project.dto.WorkoutCreateUpdateRequest;
import com.example.training_project.dto.WorkoutDto;
import com.example.training_project.dto.WorkoutWithExercisesRequest;
import com.example.training_project.service.WorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@Tag(name = "Workouts", description = "CRUD, поиск и транзакционные сценарии для тренировок")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(final WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @GetMapping
    @Operation(summary = "Получить список тренировок")
    public List<WorkoutDto> getAll(
            @Parameter(description = "Фильтр по типу тренировки")
            @RequestParam(required = false) final String type
    ) {
        return workoutService.getWorkouts(type);
    }

    @GetMapping("/search/jpql")
    @Operation(summary = "Поиск тренировок через JPQL")
    public Page<WorkoutDto> searchJpql(
            @RequestParam(required = false) final String type,
            @RequestParam(required = false) final String coachName,
            @RequestParam(required = false) final String programName,
            @PageableDefault(size = 20) final Pageable pageable
    ) {
        return workoutService.searchWorkoutsJpql(type, coachName, programName, pageable);
    }

    @GetMapping("/search/native")
    @Operation(summary = "Поиск тренировок через native query")
    public Page<WorkoutDto> searchNative(
            @RequestParam(required = false) final String coachName,
            @RequestParam(required = false) final String programName,
            final Pageable pageable
    ) {
        return workoutService.searchWorkoutsNative(coachName, programName, pageable);
    }

    @GetMapping("/optimized")
    @Operation(summary = "Поиск тренировок через native query")
    public List<WorkoutDto> getAllOptimized() {
        return workoutService.getAllWorkoutsOptimized();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить тренировку по id")
    public WorkoutDto getById(@PathVariable final Long id) {
        return workoutService.getWorkoutById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать тренировку")
    public WorkoutDto create(@Valid @RequestBody final WorkoutCreateUpdateRequest request) {
        return workoutService.createWorkout(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить тренировку")
    public WorkoutDto update(@PathVariable final Long id,
                             @Valid @RequestBody final WorkoutCreateUpdateRequest request) {
        return workoutService.updateWorkout(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить тренировку")
    public void delete(@PathVariable final Long id) {
        workoutService.deleteWorkout(id);
    }

    @GetMapping("/persisted_counts")
    @Operation(summary = "Получить количество записей в основных таблицах")
    public String getPersistedCounts() {
        return workoutService.getPersistedCounts();
    }

    @PostMapping("/with_exercises")
    @Operation(summary = "Создать тренировку вместе с новыми упражнениями")
    public ResponseEntity<WorkoutDto> addWorkoutWithExercises(
            @Valid @RequestBody final WorkoutWithExercisesRequest request
    ) {
        WorkoutDto created = workoutService.addWorkoutWithExercises(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/with_exercises/bulk/transactional")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Массово создать тренировки и упражнения (в одной транзакции)")
    public List<WorkoutDto> addWorkoutsWithExercisesBulkTransactional(
            @RequestBody final List<@Valid WorkoutWithExercisesRequest> requests
    ) {
        return workoutService.addWorkoutsWithExercisesBulkTransactional(requests);
    }

    @PostMapping("/with_exercises/bulk/non_transactional")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Массово создать тренировки и упражнения (без общей транзакции)")
    public List<WorkoutDto> addWorkoutsWithExercisesBulkNonTransactional(
            @RequestBody final List<@Valid WorkoutWithExercisesRequest> requests
    ) {
        return workoutService.addWorkoutsWithExercisesBulkNonTransactional(requests);
    }
}
