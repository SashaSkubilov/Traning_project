package com.example.training_project.service;

import com.example.training_project.dto.AsyncTaskCreateResponse;
import com.example.training_project.dto.AsyncTaskStatusResponse;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AsyncBusinessOperationService {

    private final Map<String, AsyncTaskState> taskStorage = new ConcurrentHashMap<>();

    private final AsyncBusinessOperationExecutor asyncBusinessOperationExecutor;

    public AsyncBusinessOperationService(final AsyncBusinessOperationExecutor asyncBusinessOperationExecutor) {
        this.asyncBusinessOperationExecutor = asyncBusinessOperationExecutor;
    }

    public AsyncTaskCreateResponse startOperation() {
        String taskId = UUID.randomUUID().toString();
        taskStorage.put(taskId, AsyncTaskState.pending());
        asyncBusinessOperationExecutor.executeOperation(taskId, taskStorage);
        return new AsyncTaskCreateResponse(taskId);
    }

    public AsyncTaskStatusResponse getStatus(final String taskId) {
        AsyncTaskState state = taskStorage.get(taskId);
        if (state == null) {
            return new AsyncTaskStatusResponse(taskId, AsyncTaskStatus.FAILED,
                    null, "Task not found");
        }
        return new AsyncTaskStatusResponse(taskId, state.status(), state.result(), state.error());
    }
}
