package com.example.training_project.controller;

import com.example.training_project.dto.AsyncTaskCreateResponse;
import com.example.training_project.dto.AsyncTaskStatusResponse;
import com.example.training_project.dto.RaceConditionDemoResponse;
import com.example.training_project.service.AsyncBusinessOperationService;
import com.example.training_project.service.RaceConditionDemoService;
import com.example.training_project.service.ThreadSafeCounterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller with async and concurrency demos.
 */
@RestController
@RequestMapping("/api/concurrency")
@Tag(name = "Concurrency", description = "Асинхронные операции, потокобезопасный счётчик, race condition")
public class ConcurrencyController {

    private final AsyncBusinessOperationService asyncBusinessOperationService;
    private final ThreadSafeCounterService threadSafeCounterService;
    private final RaceConditionDemoService raceConditionDemoService;

    public ConcurrencyController(final AsyncBusinessOperationService asyncBusinessOperationService,
                                 final ThreadSafeCounterService threadSafeCounterService,
                                 final RaceConditionDemoService raceConditionDemoService) {
        this.asyncBusinessOperationService = asyncBusinessOperationService;
        this.threadSafeCounterService = threadSafeCounterService;
        this.raceConditionDemoService = raceConditionDemoService;
    }

    @PostMapping("/tasks")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Запустить асинхронную бизнес-операцию")
    public AsyncTaskCreateResponse startAsyncTask() {
        return asyncBusinessOperationService.startOperation();
    }

    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "Получить статус асинхронной бизнес-операции")
    public AsyncTaskStatusResponse getTaskStatus(@PathVariable final String taskId) {
        return asyncBusinessOperationService.getStatus(taskId);
    }

    @PostMapping("/counter/increment")
    @Operation(summary = "Увеличить потокобезопасный счётчик")
    public Map<String, Long> incrementCounter(@RequestParam(defaultValue = "1") final int times) {
        long currentValue = threadSafeCounterService.incrementMany(times);
        return Map.of("counter", currentValue);
    }

    @GetMapping("/counter")
    @Operation(summary = "Получить значение потокобезопасного счётчика")
    public Map<String, Long> getCounter() {
        return Map.of("counter", threadSafeCounterService.getValue());
    }

    @PostMapping("/counter/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Сбросить потокобезопасный счётчик")
    public void resetCounter() {
        threadSafeCounterService.reset();
    }

    @GetMapping("/race-condition")
    @Operation(summary = "Демонстрация race condition и решения на Atomic")
    public RaceConditionDemoResponse runRaceConditionDemo(
            @RequestParam(defaultValue = "64") final int threads,
            @RequestParam(defaultValue = "10000") final int incrementsPerThread
    ) {
        return raceConditionDemoService.runDemo(threads, incrementsPerThread);
    }
}
