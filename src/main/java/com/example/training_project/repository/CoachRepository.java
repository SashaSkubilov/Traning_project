package com.example.training_project.repository;

import com.example.training_project.entity.Coach;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoachRepository extends JpaRepository<Coach, Long> {

    @Override
    @EntityGraph(attributePaths = "athletes")
    List<Coach> findAll();

    @Override
    @EntityGraph(attributePaths = "athletes")
    Optional<Coach> findById(Long id);
}
