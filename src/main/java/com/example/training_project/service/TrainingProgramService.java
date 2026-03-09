package com.example.training_project.service;

import com.example.training_project.dto.TrainingProgramDto;
import com.example.training_project.entity.TrainingProgram;
import com.example.training_project.mapper.TrainingProgramMapper;
import com.example.training_project.repository.TrainingProgramRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainingProgramService {

    private final TrainingProgramRepository trainingProgramRepository;
    private final TrainingProgramMapper trainingProgramMapper;

    public TrainingProgramService(
            final TrainingProgramRepository trainingProgramRepository,
            final TrainingProgramMapper trainingProgramMapper
    ) {
        this.trainingProgramRepository = trainingProgramRepository;
        this.trainingProgramMapper = trainingProgramMapper;
    }

    @Transactional(readOnly = true)
    public List<TrainingProgramDto> getAll() {
        return trainingProgramRepository.findAll().stream()
                .map(trainingProgramMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TrainingProgramDto getById(final Long id) {
        TrainingProgram program = trainingProgramRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Program not found: " + id));
        return trainingProgramMapper.toDto(program);
    }

    @Transactional
    public TrainingProgramDto create(final TrainingProgram program) {
        return trainingProgramMapper.toDto(trainingProgramRepository.save(program));
    }

    @Transactional
    public TrainingProgramDto update(final Long id, final TrainingProgram program) {
        TrainingProgram existing = trainingProgramRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Program not found: " + id));

        existing.setName(program.getName());

        return trainingProgramMapper.toDto(trainingProgramRepository.save(existing));
    }

    @Transactional
    public void delete(final Long id) {
        trainingProgramRepository.deleteById(id);
    }
}

