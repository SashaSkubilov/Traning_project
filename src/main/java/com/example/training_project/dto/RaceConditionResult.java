package com.example.training_project.dto;

public record RaceConditionResult(String label, int threads, int incrementsPerThread,
                                  int expected, int actual, int lostUpdates) {
}
