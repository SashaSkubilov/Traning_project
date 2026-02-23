package com.example.training_project.repository;

import com.example.training_project.entity.Workout;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class WorkoutRepository {
    private final Map<Long, Workout> storage = new ConcurrentHashMap<>();

    public WorkoutRepository() {
        storage.put(1L, new Workout(1L, "Силовая", 60, LocalDateTime.now().minusDays(1)));
        storage.put(2L, new Workout(2L, "Кардио", 30, LocalDateTime.now()));
    }

    public List<Workout> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<Workout> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }
}
