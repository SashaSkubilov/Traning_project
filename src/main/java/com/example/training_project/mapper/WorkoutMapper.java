package com.example.training_project.mapper;

import com.example.training_project.dto.WorkoutDto;
import com.example.training_project.entity.Workout;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;

@Component
public class WorkoutMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public WorkoutDto toDto(Workout workout) {
        return new WorkoutDto(
                workout.getId(),
                workout.getType(),
                workout.getDurationMinutes(),
                workout.getDate().format(FORMATTER)
        );
    }
}
