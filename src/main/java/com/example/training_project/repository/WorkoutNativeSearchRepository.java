package com.example.training_project.repository;

import com.example.training_project.dto.WorkoutDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WorkoutNativeSearchRepository {

    Page<WorkoutDto> findByFiltersNative(String coachName,
                                      String programName,
                                      Pageable pageable);
}
