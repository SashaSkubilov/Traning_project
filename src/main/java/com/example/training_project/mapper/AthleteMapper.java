package com.example.training_project.mapper;

import com.example.training_project.dto.AthleteDto;
import com.example.training_project.entity.Athlete;
import org.springframework.stereotype.Component;

@Component
public class AthleteMapper {

    public AthleteDto toDto(final Athlete athlete) {
        return new AthleteDto(
                athlete.getId(),
                athlete.getFirstName(),
                athlete.getLastName(),
                athlete.getCoach() != null ? athlete.getCoach().getId() : null,
                athlete.getCoach() != null
                        ? athlete.getCoach().getFirstName() + " " + athlete.getCoach().getLastName()
                        : null
        );
    }
}

