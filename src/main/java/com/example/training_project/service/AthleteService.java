package com.example.training_project.service;

import com.example.training_project.dto.AthleteDto;
import com.example.training_project.entity.Athlete;
import com.example.training_project.mapper.AthleteMapper;
import com.example.training_project.repository.AthleteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AthleteService {

    private final AthleteRepository athleteRepository;
    private final AthleteMapper athleteMapper;

    public AthleteService(final AthleteRepository athleteRepository, final AthleteMapper athleteMapper) {
        this.athleteRepository = athleteRepository;
        this.athleteMapper = athleteMapper;
    }

    @Transactional(readOnly = true)
    public List<AthleteDto> getAll() {
        return athleteRepository.findAll().stream()
                .map(athleteMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AthleteDto getById(final Long id) {
        Athlete athlete = athleteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Athlete not found: " + id));
        return athleteMapper.toDto(athlete);
    }

    @Transactional
    public AthleteDto create(final Athlete athlete) {
        return athleteMapper.toDto(athleteRepository.save(athlete));
    }

    @Transactional
    public AthleteDto update(final Long id, final Athlete athlete) {
        Athlete existing = athleteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Athlete not found: " + id));

        existing.setFirstName(athlete.getFirstName());
        existing.setLastName(athlete.getLastName());
        existing.setCoach(athlete.getCoach());

        return athleteMapper.toDto(athleteRepository.save(existing));
    }

    @Transactional
    public void delete(final Long id) {
        athleteRepository.deleteById(id);
    }
}

