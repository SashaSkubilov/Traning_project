package com.example.training_project.service;

import com.example.training_project.dto.RaceConditionDemoResponse;
import com.example.training_project.dto.RaceConditionResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RaceConditionDemoService {

    private static final int AWAIT_SECONDS = 30;

    public RaceConditionDemoResponse runDemo(final int threads, final int incrementsPerThread) {
        RaceConditionResult unsafe = runUnsafeCounterScenario(threads, incrementsPerThread);
        RaceConditionResult safe = runSafeCounterScenario(threads, incrementsPerThread);
        return new RaceConditionDemoResponse(unsafe, safe);
    }

    private RaceConditionResult runUnsafeCounterScenario(final int threads,
                                                         final int incrementsPerThread) {
        int expected = threads * incrementsPerThread;
        int[] counter = new int[]{0};
        runConcurrentWork(threads, incrementsPerThread, () -> counter[0]++);
        int actual = counter[0];
        return new RaceConditionResult("unsafe", threads, incrementsPerThread,
                expected, actual, expected - actual);
    }

    private RaceConditionResult runSafeCounterScenario(final int threads,
                                                       final int incrementsPerThread) {
        int expected = threads * incrementsPerThread;
        AtomicInteger counter = new AtomicInteger(0);
        runConcurrentWork(threads, incrementsPerThread, counter::incrementAndGet);
        int actual = counter.get();
        return new RaceConditionResult("atomic", threads, incrementsPerThread,
                expected, actual, expected - actual);
    }

    private void runConcurrentWork(final int threads, final int incrementsPerThread,
                                   final Runnable incrementAction) {
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch countDownLatch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        incrementAction.run();
                    }
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        try {
            boolean completed = countDownLatch.await(AWAIT_SECONDS, TimeUnit.SECONDS);
            if (!completed) {
                throw new IllegalStateException("Threads did not finish in time");
            }
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Race demo interrupted", interruptedException);
        } finally {
            executorService.shutdown();
        }
    }
}
