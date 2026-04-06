package com.example.training_project.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class ThreadSafeCounterService {

    private final AtomicLong counter = new AtomicLong(0L);

    public long incrementAndGet() {
        return counter.incrementAndGet();
    }

    public long incrementMany(final int times) {
        long value = 0L;
        for (int i = 0; i < times; i++) {
            value = counter.incrementAndGet();
        }
        return value;
    }

    public long getValue() {
        return counter.get();
    }

    public void reset() {
        counter.set(0L);
    }
}
