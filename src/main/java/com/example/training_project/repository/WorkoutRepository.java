package com.example.training_project.repository;

import com.example.training_project.entity.Workout;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

/**
 * Repository for Workout entities with N+1 problem solutions.
 */
public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    @EntityGraph(attributePaths = {"athlete", "program", "exercises"})
    @Query("select w from Workout w")
    List<Workout> findAllWithDetails();
}
