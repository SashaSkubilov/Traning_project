package com.example.training_project.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncBusinessOperationExecutor {

    @Async
    public CompletableFuture<Void> executeOperation(final String taskId,
                                                    final Map<String, AsyncTaskState> taskStorage) {
        taskStorage.put(taskId, AsyncTaskState.running());
        try {
            Thread.sleep(20000);
            String result = "Business operation completed at " + Instant.now();
            taskStorage.put(taskId, AsyncTaskState.completed(result));
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            taskStorage.put(taskId, AsyncTaskState.failed("Task interrupted"));
        } catch (RuntimeException runtimeException) {
            taskStorage.put(taskId, AsyncTaskState.failed(runtimeException.getMessage()));
        }
        return CompletableFuture.completedFuture(null);
    }
}
