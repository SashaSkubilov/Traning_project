package com.example.training_project.repository;

import com.example.training_project.entity.TrainingProgram;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrainingProgramRepository extends JpaRepository<TrainingProgram, Long> {

    @Override
    @EntityGraph(attributePaths = "workouts")
    List<TrainingProgram> findAll();

    @Override
    @EntityGraph(attributePaths = "workouts")
    Optional<TrainingProgram> findById(Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
