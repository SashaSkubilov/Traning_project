package com.example.training_project.service;

public record AsyncTaskState(AsyncTaskStatus status, String result, String error) {

    public static AsyncTaskState pending() {
        return new AsyncTaskState(AsyncTaskStatus.PENDING, null, null);
    }

    public static AsyncTaskState running() {
        return new AsyncTaskState(AsyncTaskStatus.RUNNING, null, null);
    }

    public static AsyncTaskState completed(final String result) {
        return new AsyncTaskState(AsyncTaskStatus.COMPLETED, result, null);
    }

    public static AsyncTaskState failed(final String error) {
        return new AsyncTaskState(AsyncTaskStatus.FAILED, null, error);
    }
}
