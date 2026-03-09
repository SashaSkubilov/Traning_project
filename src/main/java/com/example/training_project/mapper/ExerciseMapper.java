package com.example.training_project.mapper;

import com.example.training_project.dto.ExerciseDto;
import com.example.training_project.entity.Exercise;
import org.springframework.stereotype.Component;

@Component
public class ExerciseMapper {

    public ExerciseDto toDto(final Exercise exercise) {
        return new ExerciseDto(
                exercise.getId(),
                exercise.getName()
        );
    }
}

