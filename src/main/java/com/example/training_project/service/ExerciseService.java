package com.example.training_project.service;

import com.example.training_project.dto.ExerciseCreateUpdateRequest;
import com.example.training_project.dto.ExerciseDto;
import com.example.training_project.entity.Exercise;
import com.example.training_project.mapper.ExerciseMapper;
import com.example.training_project.repository.ExerciseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;

    private final ExerciseMapper exerciseMapper;

    public ExerciseService(final ExerciseRepository exerciseRepository, final ExerciseMapper exerciseMapper) {
        this.exerciseRepository = exerciseRepository;
        this.exerciseMapper = exerciseMapper;
    }

    @Transactional(readOnly = true)
    public List<ExerciseDto> getAll() {
        return exerciseRepository.findAll().stream()
                .map(exerciseMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExerciseDto getById(final Long id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exercise not found: " + id));
        return exerciseMapper.toDto(exercise);
    }

    @Transactional
    public ExerciseDto create(final ExerciseCreateUpdateRequest request) {
        Exercise exercise = new Exercise();
        exercise.setName(request.name());
        return exerciseMapper.toDto(exerciseRepository.save(exercise));
    }

    @Transactional
    public ExerciseDto update(final Long id, final ExerciseCreateUpdateRequest request) {
        Exercise existing = exerciseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exercise not found: " + id));

        existing.setName(request.name());

        return exerciseMapper.toDto(exerciseRepository.save(existing));
    }

    @Transactional
    public void delete(final Long id) {
        exerciseRepository.deleteById(id);
    }
}

