package com.example.training_project.repository;

import com.example.training_project.entity.Workout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WorkoutNativeSearchRepository {

    Page<Workout> findByFiltersNative(Long coachId,
                                      Long programId,
                                      Pageable pageable);
}
