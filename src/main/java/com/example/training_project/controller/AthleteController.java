package com.example.training_project.controller;

import com.example.training_project.dto.AthleteDto;
import com.example.training_project.entity.Athlete;
import com.example.training_project.service.AthleteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/athletes")
public class AthleteController {

    private final AthleteService athleteService;

    public AthleteController(final AthleteService athleteService) {
        this.athleteService = athleteService;
    }

    @GetMapping
    public List<AthleteDto> getAll() {
        return athleteService.getAll();
    }

    @GetMapping("/{id}")
    public AthleteDto getById(@PathVariable final Long id) {
        return athleteService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AthleteDto create(@RequestBody final Athlete athlete) {
        return athleteService.create(athlete);
    }

    @PutMapping("/{id}")
    public AthleteDto update(@PathVariable final Long id, @RequestBody final Athlete athlete) {
        return athleteService.update(id, athlete);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final Long id) {
        athleteService.delete(id);
    }
}

