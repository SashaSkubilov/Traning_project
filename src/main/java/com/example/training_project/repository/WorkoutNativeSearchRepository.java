package com.example.training_project.repository;

import com.example.training_project.entity.Workout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * PostgreSQL-specific native workout search with safe sort mapping.
 */
public interface WorkoutNativeSearchRepository {

    default Page<Workout> findByFiltersNative(final Long coachId,
                                              final Long programId,
                                              final Pageable pageable) {
        return findByFiltersNative(coachId, programId, pageable);
    }
}
