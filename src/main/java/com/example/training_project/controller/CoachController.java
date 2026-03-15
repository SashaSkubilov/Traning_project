package com.example.training_project.controller;

import com.example.training_project.dto.CoachCreateUpdateRequest;
import com.example.training_project.dto.CoachDto;
import com.example.training_project.service.CoachService;
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
@RequestMapping("/api/coaches")
public class CoachController {

    private final CoachService coachService;

    public CoachController(final CoachService coachService) {
        this.coachService = coachService;
    }

    @GetMapping
    public List<CoachDto> getAll() {
        return coachService.getAll();
    }

    @GetMapping("/{id}")
    public CoachDto getById(@PathVariable final Long id) {
        return coachService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CoachDto create(@RequestBody final CoachCreateUpdateRequest request) {
        return coachService.create(request);
    }

    @PutMapping("/{id}")
    public CoachDto update(@PathVariable final Long id,
                           @RequestBody final CoachCreateUpdateRequest request) {
        return coachService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final Long id) {
        coachService.delete(id);
    }
}
