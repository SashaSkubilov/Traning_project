package com.example.training_project.service;

import com.example.training_project.dto.AthleteCreateUpdateRequest;
import com.example.training_project.dto.AthleteDto;
import com.example.training_project.entity.Athlete;
import com.example.training_project.entity.Coach;
import com.example.training_project.mapper.AthleteMapper;
import com.example.training_project.repository.AthleteRepository;
import com.example.training_project.repository.CoachRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AthleteService {

    private final AthleteRepository athleteRepository;

    private final CoachRepository coachRepository;

    private final AthleteMapper athleteMapper;

    public AthleteService(
            final AthleteRepository athleteRepository,
            final CoachRepository coachRepository,
            final AthleteMapper athleteMapper
    ) {
        this.athleteRepository = athleteRepository;
        this.coachRepository = coachRepository;
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
    public AthleteDto create(final AthleteCreateUpdateRequest request) {
        Athlete athlete = new Athlete();
        applyRequestToEntity(athlete, request);
        return athleteMapper.toDto(athleteRepository.save(athlete));
    }

    @Transactional
    public AthleteDto update(final Long id, final AthleteCreateUpdateRequest request) {
        Athlete existing = athleteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Athlete not found: " + id));

        applyRequestToEntity(existing, request);

        return athleteMapper.toDto(athleteRepository.save(existing));
    }

    private void applyRequestToEntity(final Athlete athlete, final AthleteCreateUpdateRequest request) {
        athlete.setFirstName(request.firstName());
        athlete.setLastName(request.lastName());

        if (request.coachId() != null) {
            Coach coach = coachRepository.findById(request.coachId())
                    .orElseThrow(() -> new EntityNotFoundException("Coach not found: " + request.coachId()));
            athlete.setCoach(coach);
        } else {
            athlete.setCoach(null);
        }
    }

    @Transactional
    public void delete(final Long id) {
        athleteRepository.deleteById(id);
    }
}

