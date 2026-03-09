package com.example.training_project.service;

import com.example.training_project.dto.CoachDto;
import com.example.training_project.entity.Coach;
import com.example.training_project.mapper.CoachMapper;
import com.example.training_project.repository.CoachRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CoachService {

    private final CoachRepository coachRepository;
    private final CoachMapper coachMapper;

    public CoachService(final CoachRepository coachRepository, final CoachMapper coachMapper) {
        this.coachRepository = coachRepository;
        this.coachMapper = coachMapper;
    }

    @Transactional(readOnly = true)
    public List<CoachDto> getAll() {
        return coachRepository.findAll().stream()
                .map(coachMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CoachDto getById(final Long id) {
        Coach coach = coachRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Coach not found: " + id));
        return coachMapper.toDto(coach);
    }

    @Transactional
    public CoachDto create(final Coach coach) {
        return coachMapper.toDto(coachRepository.save(coach));
    }

    @Transactional
    public CoachDto update(final Long id, final Coach coach) {
        Coach existing = coachRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Coach not found: " + id));

        existing.setFirstName(coach.getFirstName());
        existing.setLastName(coach.getLastName());

        return coachMapper.toDto(coachRepository.save(existing));
    }

    @Transactional
    public void delete(final Long id) {
        coachRepository.deleteById(id);
    }
}

