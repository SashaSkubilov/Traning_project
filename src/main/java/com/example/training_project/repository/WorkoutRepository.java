package com.example.training_project.repository;

import com.example.training_project.entity.Workout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkoutRepository extends JpaRepository<Workout, Long>, WorkoutNativeSearchRepository {

    @Override
    @EntityGraph(attributePaths = {"athlete", "program", "exercises"})
    List<Workout> findAll();

    @Override
    @EntityGraph(attributePaths = {"athlete", "program", "exercises"})
    Optional<Workout> findById(Long id);

    @EntityGraph(attributePaths = {"athlete", "program", "exercises"})
    @Query("select distinct w from Workout w")
    List<Workout> findAllWithDetails();

    @EntityGraph(attributePaths = {"athlete", "program", "exercises"})
    @Query("select distinct w from Workout w where w.id in :ids")
    List<Workout> findAllWithDetailsByIdIn(@Param("ids") List<Long> ids);

    @EntityGraph(attributePaths = {"athlete", "program", "exercises"})
    @Query(value = """
    select distinct w from Workout w
    join w.athlete a
    join a.coach c
    join w.program p
    where (:coachName is null or lower(concat(c.firstName, ' ', c.lastName)) = lower(:coachName))
    and (:programName is null or lower(p.name) = lower(:programName))
    and (:type is null or lower(w.type) = lower(:type))
            """,
            countQuery = """
    select count(distinct w.id) from Workout w
    join w.athlete a
    join a.coach c
    join w.program p
    where (:coachName is null or lower(concat(c.firstName, ' ', c.lastName)) = lower(:coachName))
    and (:programName is null or lower(p.name) = lower(:programName))
    and (:type is null or lower(w.type) = lower(:type))
            """)
    Page<Workout> findByFiltersJpql(
            @Param("type") String type,
            @Param("coachName") String coachName,
            @Param("programName") String programName,
            Pageable pageable
    );
}
