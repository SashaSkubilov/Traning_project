package com.example.training_project.repository;

import com.example.training_project.entity.Athlete;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AthleteRepository extends JpaRepository<Athlete, Long> {

    @Override
    @EntityGraph(attributePaths = "coach")
    List<Athlete> findAll();

    @Override
    @EntityGraph(attributePaths = "coach")
    Optional<Athlete> findById(Long id);
}
