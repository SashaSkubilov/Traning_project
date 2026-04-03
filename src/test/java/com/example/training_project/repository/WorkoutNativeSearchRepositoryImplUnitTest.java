package com.example.training_project.repository;

import com.example.training_project.dto.WorkoutDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkoutNativeSearchRepositoryImplUnitTest {

    @Mock
    private EntityManager entityManager;
    @Mock
    private Query contentQuery;
    @Mock
    private Query countQuery;

    private WorkoutNativeSearchRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new WorkoutNativeSearchRepositoryImpl();
        ReflectionTestUtils.setField(repository, "entityManager", entityManager);
    }

    @Test
    void shouldBindSortAndMapRowsWithNullValues() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(contentQuery, countQuery);
        when(contentQuery.setParameter(anyString(), any())).thenReturn(contentQuery);
        when(contentQuery.setFirstResult(5)).thenReturn(contentQuery);
        when(contentQuery.setMaxResults(5)).thenReturn(contentQuery);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);

        Object[] first = {
                1L, "Title", "Type", 35, Timestamp.valueOf(LocalDateTime.now()), "Athlete", "Prog", 2
        };
        Object[] second = {2L, "Second", "Type", null, null, "Athlete", "Prog", null};
        when(contentQuery.getResultList()).thenReturn(List.of(first, second));
        when(countQuery.getSingleResult()).thenReturn(7L);

        Page<WorkoutDto> page = repository.findByFiltersNative(
                "Coach Name",
                "Mass",
                PageRequest.of(1, 5, Sort.by(Sort.Order.desc("unknown"), Sort.Order.asc("title")))
        );

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(1).durationMinutes()).isNull();
        assertThat(page.getContent().get(1).scheduledAt()).isNull();
        assertThat(page.getContent().get(1).exercisesCount()).isZero();
        assertThat(page.getTotalElements()).isGreaterThan(0);

        verify(contentQuery).setFirstResult(5);
        verify(contentQuery).setMaxResults(5);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(contentQuery, Mockito.atLeast(2)).setParameter(keyCaptor.capture(), any());
        assertThat(keyCaptor.getAllValues())
                .contains("sort0Property", "sort0Direction", "sort1Property");
    }

    @Test
    void shouldSkipPaginationForUnpagedRequests() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(contentQuery, countQuery);
        when(contentQuery.setParameter(anyString(), any())).thenReturn(contentQuery);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(contentQuery.getResultList()).thenReturn(List.of());
        when(countQuery.getSingleResult()).thenReturn(0L);

        Page<WorkoutDto> page = repository.findByFiltersNative("Coach", "Program", Pageable.unpaged());

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }

    @Test
    void shouldLimitSortBindingsToMaximumSupportedFields() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(contentQuery, countQuery);
        when(contentQuery.setParameter(anyString(), any())).thenReturn(contentQuery);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(contentQuery.setFirstResult(0)).thenReturn(contentQuery);
        when(contentQuery.setMaxResults(10)).thenReturn(contentQuery);
        when(contentQuery.getResultList()).thenReturn(List.of());
        when(countQuery.getSingleResult()).thenReturn(0L);

        Sort oversizedSort = Sort.by(
                Sort.Order.asc("id"),
                Sort.Order.desc("title"),
                Sort.Order.asc("durationMinutes"),
                Sort.Order.desc("scheduledAt"),
                Sort.Order.asc("title")
        );

        repository.findByFiltersNative("Coach", "Program", PageRequest.of(0, 10, oversizedSort));

        verify(contentQuery).setParameter("sort3Property", "scheduledAt");
    }
}
