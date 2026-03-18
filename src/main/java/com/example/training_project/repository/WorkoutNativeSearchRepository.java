package com.example.training_project.repository;

import com.example.training_project.entity.Workout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * PostgreSQL-specific native workout search with safe sort mapping.
 */
public interface WorkoutNativeSearchRepository {

    Page<Workout> findByFiltersNative(Long coachId,
                                      Long programId,
                                      Pageable pageable);
}
