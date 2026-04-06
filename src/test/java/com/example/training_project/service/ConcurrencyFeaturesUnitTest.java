package com.example.training_project.service;

import com.example.training_project.dto.RaceConditionDemoResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConcurrencyFeaturesUnitTest {

    @Test
    void shouldIncrementThreadSafeCounter() {
        ThreadSafeCounterService counterService = new ThreadSafeCounterService();

        long value = counterService.incrementMany(3);

        assertThat(value).isEqualTo(3);
        assertThat(counterService.getValue()).isEqualTo(3);
    }

    @Test
    void shouldDemonstrateRaceConditionAndAtomicSolution() {
        RaceConditionDemoService raceConditionDemoService = new RaceConditionDemoService();

        RaceConditionDemoResponse response = raceConditionDemoService.runDemo(64, 3000);

        assertThat(response.safe().actual()).isEqualTo(response.safe().expected());
        assertThat(response.unsafe().actual()).isLessThanOrEqualTo(response.unsafe().expected());
    }

}
