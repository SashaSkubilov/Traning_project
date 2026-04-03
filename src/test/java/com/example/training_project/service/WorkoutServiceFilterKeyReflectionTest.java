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
        Object withDifferentPageSize = forQuery.invoke(
                null,
                jpqlType,
                "Strength\nType",
                "Coach\rName",
                null,
                PageRequest.of(0, 10, Sort.unsorted())
        );
        Object nativeType = forQuery.invoke(
                null,
                Enum.valueOf((Class<Enum>) queryTypeClass.asSubclass(Enum.class), "NATIVE"),
                "Strength\nType",
                "Coach\rName",
                null,
                PageRequest.of(0, 20, Sort.unsorted())
        );
        Object withDifferentWorkoutType = forQuery.invoke(
                null,
                jpqlType,
                "Cardio",
                "Coach\rName",
                null,
                PageRequest.of(0, 20, Sort.unsorted())
        );
        Object withDifferentCoach = forQuery.invoke(
                null,
                jpqlType,
                "Strength\nType",
                "Other coach",
                null,
                PageRequest.of(0, 20, Sort.unsorted())
        );
        Object withDifferentProgram = forQuery.invoke(
                null,
                jpqlType,
                "Strength\nType",
                "Coach\rName",
                "Other program",
                PageRequest.of(0, 20, Sort.unsorted())
        );
        Object withDifferentSortOnly = forQuery.invoke(
                null,
                jpqlType,
                "Strength\nType",
                "Coach\rName",
                null,
                PageRequest.of(0, 20, Sort.by("scheduledAt").ascending())
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
        assertThat(first).isNotEqualTo(withDifferentPageSize);
        assertThat(first).isNotEqualTo(nativeType);
        assertThat(first).isNotEqualTo(withDifferentWorkoutType);
        assertThat(first).isNotEqualTo(withDifferentCoach);
        assertThat(first).isNotEqualTo(withDifferentProgram);
        assertThat(first).isNotEqualTo(withDifferentSortOnly);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(logSafe).doesNotContain("\n").doesNotContain("\r");
        assertThat(logSafe).contains("Strength_Type").contains("Coach_Name");
        assertThat(sortedLogSafe).contains("title: DESC");

        Method sanitizeForLog = keyClass.getDeclaredMethod("sanitizeForLog", String.class);
        sanitizeForLog.setAccessible(true);
        assertThat((String) sanitizeForLog.invoke(null, (Object) null)).isNull();
        assertThat((String) sanitizeForLog.invoke(null, "A\r\nB")).isEqualTo("A__B");

        Method values = queryTypeClass.getDeclaredMethod("values");
        Object enumValues = values.invoke(null);
        assertThat((Object[]) enumValues).hasSize(2);

        Method valueOf = queryTypeClass.getDeclaredMethod("valueOf", String.class);
        assertThat(valueOf.invoke(null, "NATIVE").toString()).isEqualTo("NATIVE");
    }
}
