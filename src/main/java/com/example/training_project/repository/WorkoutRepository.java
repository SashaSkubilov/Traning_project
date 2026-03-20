package com.example.training_project.repository;

import com.example.training_project.entity.Workout;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WorkoutRepository extends JpaRepository<Workout, Long>, WorkoutNativeSearchRepository {

    @EntityGraph(attributePaths = {"athlete", "program", "exercises"})
    @Query("select w from Workout w")
    List<Workout> findAllWithDetails();

    @Query("""
            select w from Workout w
            join w.athlete a
            join a.coach c
            join w.program p
            where (:coachName is null or lower(concat(c.firstName, ' ', c.lastName)) = lower(:coachName))
                and (:programName is null or lower(p.name) = lower(:programName))
            """)
    Page<Workout> findByFiltersJpql(
            @Param("coachName") String coachName,
            @Param("programName") String programName,
            Pageable pageable
    );
}
