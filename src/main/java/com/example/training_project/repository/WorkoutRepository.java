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
            where (:coachId is null or c.id = :coachId)
              and (:programId is null or p.id = :programId)
            """)
    Page<Workout> findByFiltersJpql(
            @Param("coachId") Long coachId,
            @Param("programId") Long programId,
            Pageable pageable
    );
}
