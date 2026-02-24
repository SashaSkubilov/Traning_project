package com.example.training_project.entity;

import java.time.LocalDateTime;

public class Workout {
    private Long id;

    private String type;

    private Integer durationMinutes;

    private LocalDateTime date;

    public Workout(Long id, String type, Integer durationMinutes, LocalDateTime date) {
        this.id = id;
        this.type = type;
        this.durationMinutes = durationMinutes;
        this.date = date;
    }

    public Long getId() {
        return id; }

    public String getType() {
        return type; }

    public Integer getDurationMinutes() {
        return durationMinutes; }

    public LocalDateTime getDate() {
        return date; }
}
