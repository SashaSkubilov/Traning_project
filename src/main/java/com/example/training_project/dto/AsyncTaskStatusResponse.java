package com.example.training_project.dto;

import com.example.training_project.service.AsyncTaskStatus;

public record AsyncTaskStatusResponse(String taskId, AsyncTaskStatus status,
                                      String result, String error) {
}
