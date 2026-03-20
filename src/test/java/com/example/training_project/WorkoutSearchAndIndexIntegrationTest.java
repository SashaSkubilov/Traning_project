package com.example.training_project;

import com.example.training_project.dto.WorkoutCreateUpdateRequest;
import com.example.training_project.dto.WorkoutDto;
import com.example.training_project.entity.Athlete;
import com.example.training_project.entity.Exercise;
import com.example.training_project.entity.TrainingProgram;
import com.example.training_project.repository.AthleteRepository;
import com.example.training_project.repository.ExerciseRepository;
import com.example.training_project.repository.TrainingProgramRepository;
import com.example.training_project.repository.WorkoutRepository;
import com.example.training_project.service.WorkoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class WorkoutSearchAndIndexIntegrationTest {

    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private AthleteRepository athleteRepository;

    @Autowired
    private TrainingProgramRepository trainingProgramRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @MockitoSpyBean
    private WorkoutRepository workoutRepository;

    @BeforeEach
    void clearIndexAndSpyState() {
        Map<?, ?> workoutIndex = (Map<?, ?>) ReflectionTestUtils.getField(workoutService, "workoutIndex");
        assertThat(workoutIndex).isNotNull();
        workoutIndex.clear();
        clearInvocations(workoutRepository);
    }

    @Test
    void jpqlSearchFiltersByNestedEntitiesWithPagination() {
        Athlete athlete = athleteRepository.findAll().get(0);
        String coachName = athlete.getCoach().getFirstName() + " " + athlete.getCoach().getLastName();
        String programName = trainingProgramRepository.findAll().get(0).getName();
        Pageable pageable = PageRequest.of(0, 1, Sort.by("title").ascending());

        Page<WorkoutDto> result = workoutService.searchWorkoutsJpql(null, coachName, programName, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Leg Day");
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(workoutRepository, times(1)).findByFiltersJpql(null, coachName, programName, pageable);
    }

    @Test
    void nativeSearchFiltersByNestedEntitiesWithPagination() {
        Athlete athlete = athleteRepository.findAll().get(0);
        String coachName = athlete.getCoach().getFirstName() + " " + athlete.getCoach().getLastName();
        String programName = trainingProgramRepository.findAll().get(0).getName();
        Pageable pageable = PageRequest.of(0, 1, Sort.by("title").ascending());

        Page<WorkoutDto> result = workoutService.searchWorkoutsNative(coachName, programName, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Leg Day");
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(workoutRepository, times(1)).findByFiltersNative(coachName, programName, pageable);
    }

    @Test
    void nativeSearchIgnoresUnsupportedSortProperties() {
        Athlete athlete = athleteRepository.findAll().get(0);
        String coachName = athlete.getCoach().getFirstName() + " " + athlete.getCoach().getLastName();
        String programName = trainingProgramRepository.findAll().get(0).getName();
        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by(Sort.Order.asc("title; drop table workouts"), Sort.Order.asc("title"))
        );

        Page<WorkoutDto> result = workoutService.searchWorkoutsNative(coachName, programName, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Leg Day");
        verify(workoutRepository, times(1)).findByFiltersNative(coachName, programName, pageable);
    }

    @Test
    void inMemoryIndexUsesCompositeKeyAndIsInvalidatedAfterDataChanges() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("title").ascending());

        workoutService.searchWorkoutsJpql(null, null, null, pageable);
        workoutService.searchWorkoutsJpql(null, null, null, pageable);

        verify(workoutRepository, times(1)).findByFiltersJpql(null, null, null, pageable);

        Athlete athlete = athleteRepository.findAll().get(0);
        TrainingProgram program = trainingProgramRepository.findAll().get(0);
        List<Long> exerciseIds = exerciseRepository.findAll().stream().map(Exercise::getId).toList();

        WorkoutCreateUpdateRequest request = new WorkoutCreateUpdateRequest(
                "Cache invalidation check",
                "Strength",
                55,
                LocalDateTime.now().plusDays(3),
                athlete.getId(),
                program.getId(),
                exerciseIds
        );

        workoutService.createWorkout(request);
        workoutService.searchWorkoutsJpql(null, null, null, pageable);

        verify(workoutRepository, times(2)).findByFiltersJpql(null, null, null, pageable);
    }
}
