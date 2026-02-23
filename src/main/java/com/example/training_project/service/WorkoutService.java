package com.example.training_project.service;

import com.example.training_project.dto.WorkoutDto;
import com.example.training_project.mapper.WorkoutMapper;
import com.example.training_project.repository.WorkoutRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkoutService {

    private final WorkoutRepository repository;
    private final WorkoutMapper mapper;

    public WorkoutService(WorkoutRepository repository, WorkoutMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<WorkoutDto> getWorkouts(String type) {
        return repository.findAll().stream()
                .filter(w -> type == null || w.getType().equalsIgnoreCase(type))
                .map(mapper::toDto)
                .toList();
    }

    public WorkoutDto getWorkoutById(Long id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new RuntimeException("Тренировка не найдена"));
    }
}