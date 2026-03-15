package com.example.training_project.mapper;

import com.example.training_project.dto.CoachDto;
import com.example.training_project.entity.Coach;
import org.springframework.stereotype.Component;

@Component
public class CoachMapper {

    public CoachDto toDto(final Coach coach) {
        return new CoachDto(
                coach.getId(),
                coach.getFirstName(),
                coach.getLastName(),
                coach.getAthletes() != null ? coach.getAthletes().size() : 0
        );
    }
}
