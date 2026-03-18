package com.example.training_project.repository;

import com.example.training_project.entity.Workout;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Repository for Workout entities with N+1 problem solutions.
 */
public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    @EntityGraph(attributePaths = {"athlete", "program", "exercises"})
    @Query("select w from Workout w")
    List<Workout> findAllWithDetails();

    /**
     * Complex JPQL query with filtering by nested entities and pagination.
     */
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

    /**
     * The same query implemented as native SQL with pagination.
     */
    @Query(
            value = """
                    select w.*
                    from workouts w
                    join athletes a on w.athlete_id = a.id
                    join coaches c on a.coach_id = c.id
                    join programs p on w.program_id = p.id
                    where (:coachId is null or c.id = :coachId)
                      and (:programId is null or p.id = :programId)
                    """,
            countQuery = """
                    select count(*)
                    from workouts w
                    join athletes a on w.athlete_id = a.id
                    join coaches c on a.coach_id = c.id
                    join programs p on w.program_id = p.id
                    where (:coachId is null or c.id = :coachId)
                      and (:programId is null or p.id = :programId)
                    """,
            nativeQuery = true
    )
    Page<Workout> findByFiltersNative(
            @Param("coachId") Long coachId,
            @Param("programId") Long programId,
            Pageable pageable
    );
}
