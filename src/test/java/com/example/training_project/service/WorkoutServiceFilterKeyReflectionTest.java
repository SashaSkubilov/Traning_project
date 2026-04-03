package com.example.training_project.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class WorkoutServiceFilterKeyReflectionTest {

    @Test
    @SuppressWarnings("unchecked")
    void shouldCoverFilterKeyEqualityHashAndLogSanitization() throws Exception {
        Class<?> keyClass = Class.forName("com.example.training_project.service.WorkoutService$WorkoutFilterKey");
        Class<?> queryTypeClass = Class.forName(
                "com.example.training_project.service.WorkoutService$WorkoutFilterKey$QueryType"
        );

        Object jpqlType = Enum.valueOf((Class<Enum>) queryTypeClass.asSubclass(Enum.class), "JPQL");

        Method forQuery = keyClass.getDeclaredMethod(
                "forQuery",
                queryTypeClass,
                String.class,
                String.class,
                String.class,
                org.springframework.data.domain.Pageable.class
        );
        forQuery.setAccessible(true);

        Object first = forQuery.invoke(
                null,
                jpqlType,
                "Strength\nType",
                "Coach\rName",
                null,
                PageRequest.of(0, 20, Sort.unsorted())
        );
        Object second = forQuery.invoke(
                null,
                jpqlType,
                "Strength\nType",
                "Coach\rName",
                null,
                PageRequest.of(0, 20, Sort.unsorted())
        );
        Object withSortedPage = forQuery.invoke(
                null,
                jpqlType,
                "Strength\nType",
                "Coach\rName",
                null,
                PageRequest.of(1, 20, Sort.by("title").descending())
        );

        Method toLogSafeString = keyClass.getDeclaredMethod("toLogSafeString");
        toLogSafeString.setAccessible(true);
        String logSafe = (String) toLogSafeString.invoke(first);
        String sortedLogSafe = (String) toLogSafeString.invoke(withSortedPage);

        assertThat(first.equals(first)).isTrue();
        assertThat(first.equals(null)).isFalse();
        assertThat(first.equals("unexpected")).isFalse();
        assertThat(first).isEqualTo(second);
        assertThat(first).isNotEqualTo(withSortedPage);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(logSafe).doesNotContain("\n").doesNotContain("\r");
        assertThat(logSafe).contains("Strength_Type").contains("Coach_Name");
        assertThat(sortedLogSafe).contains("title: DESC");
    }
}
