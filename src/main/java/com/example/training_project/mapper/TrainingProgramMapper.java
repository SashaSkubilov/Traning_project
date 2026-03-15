package com.example.training_project.mapper;

import com.example.training_project.dto.TrainingProgramDto;
import com.example.training_project.entity.TrainingProgram;
import org.springframework.stereotype.Component;

@Component
public class TrainingProgramMapper {

    public TrainingProgramDto toDto(final TrainingProgram program) {
        return new TrainingProgramDto(
                program.getId(),
                program.getName(),
                program.getWorkouts() != null ? program.getWorkouts().size() : 0
        );
    }
}
