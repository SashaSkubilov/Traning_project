package com.example.training_project.service;

import com.example.training_project.dto.AthleteCreateUpdateRequest;
import com.example.training_project.dto.CoachCreateUpdateRequest;
import com.example.training_project.dto.ExerciseCreateUpdateRequest;
import com.example.training_project.dto.TrainingProgramCreateUpdateRequest;
import com.example.training_project.entity.Athlete;
import com.example.training_project.entity.Coach;
import com.example.training_project.entity.Exercise;
import com.example.training_project.entity.TrainingProgram;
import com.example.training_project.exception.DuplicateResourceException;
import com.example.training_project.mapper.AthleteMapper;
import com.example.training_project.mapper.CoachMapper;
import com.example.training_project.mapper.ExerciseMapper;
import com.example.training_project.mapper.TrainingProgramMapper;
import com.example.training_project.repository.AthleteRepository;
import com.example.training_project.repository.CoachRepository;
import com.example.training_project.repository.ExerciseRepository;
import com.example.training_project.repository.TrainingProgramRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrudServicesBranchUnitTest {

    @Mock
    private CoachRepository coachRepository;
    @Mock
    private AthleteRepository athleteRepository;
    @Mock
    private ExerciseRepository exerciseRepository;
    @Mock
    private TrainingProgramRepository trainingProgramRepository;

    private CoachService coachService;
    private AthleteService athleteService;
    private ExerciseService exerciseService;
    private TrainingProgramService trainingProgramService;

    @BeforeEach
    void setUp() {
        coachService = new CoachService(coachRepository, new CoachMapper());
        athleteService = new AthleteService(athleteRepository, coachRepository, new AthleteMapper());
        exerciseService = new ExerciseService(exerciseRepository, new ExerciseMapper());
        trainingProgramService = new TrainingProgramService(trainingProgramRepository, new TrainingProgramMapper());
    }

    @Test
    void coachServiceShouldCoverNotFoundAndDuplicateBranches() {
        CoachCreateUpdateRequest request = new CoachCreateUpdateRequest(" Ivan ", " Petrov ");

        when(coachRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> coachService.getById(1L))
                .isInstanceOf(EntityNotFoundException.class);

        when(coachRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(anyString(), anyString()))
                .thenReturn(true);
        assertThatThrownBy(() -> coachService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        when(coachRepository.findAll()).thenReturn(List.of(new Coach("Ivan", "Petrov")));
        coachService.getAll();
    }

    @Test
    void athleteServiceShouldCoverCoachOptionalAndDuplicateBranches() {
        AthleteCreateUpdateRequest withCoach = new AthleteCreateUpdateRequest(" Alex ", " Smirnov ", 1L);
        AthleteCreateUpdateRequest withoutCoach = new AthleteCreateUpdateRequest(" Alex ", " Smirnov ", null);

        when(athleteRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(anyString(), anyString()))
                .thenReturn(false);
        when(coachRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> athleteService.create(withCoach))
                .isInstanceOf(EntityNotFoundException.class);

        when(athleteRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(anyString(), anyString()))
                .thenReturn(true);
        assertThatThrownBy(() -> athleteService.create(withoutCoach))
                .isInstanceOf(DuplicateResourceException.class);

        Athlete existing = new Athlete("A", "B", null);
        when(athleteRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(athleteRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndIdNot(anyString(), anyString(), anyLong()))
                .thenReturn(false);
        when(athleteRepository.save(any(Athlete.class))).thenAnswer(invocation -> invocation.getArgument(0));
        athleteService.update(10L, withoutCoach);
    }

    @Test
    void exerciseServiceShouldCoverDuplicateUpdateAndNotFoundBranches() {
        when(exerciseRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> exerciseService.getById(1L)).isInstanceOf(EntityNotFoundException.class);

        when(exerciseRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);
        assertThatThrownBy(() -> exerciseService.create(new ExerciseCreateUpdateRequest(" Squat ")))
                .isInstanceOf(DuplicateResourceException.class);

        Exercise existing = new Exercise("Row");
        when(exerciseRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(exerciseRepository.existsByNameIgnoreCaseAndIdNot(anyString(), anyLong())).thenReturn(true);
        assertThatThrownBy(() -> exerciseService.update(2L, new ExerciseCreateUpdateRequest(" Row ")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void trainingProgramServiceShouldCoverDuplicateAndUpdateNotFoundBranches() {
        when(trainingProgramRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);
        assertThatThrownBy(() -> trainingProgramService.create(new TrainingProgramCreateUpdateRequest("Mass")))
                .isInstanceOf(DuplicateResourceException.class);

        when(trainingProgramRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> trainingProgramService.update(100L, new TrainingProgramCreateUpdateRequest("Mass")))
                .isInstanceOf(EntityNotFoundException.class);

        when(trainingProgramRepository.findAll()).thenReturn(List.of(new TrainingProgram("Mass")));
        trainingProgramService.getAll();
    }

    @Test
    void shouldCoverSuccessfulCreateUpdateAndDeleteBranches() {
        Coach existingCoach = new Coach("Ivan", "Petrov");
        when(coachRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(anyString(), anyString())).thenReturn(false);
        when(coachRepository.save(any(Coach.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertThat(coachService.create(new CoachCreateUpdateRequest(" Ivan ", " Petrov ")).firstName()).isEqualTo("Ivan");
        when(coachRepository.findById(1L)).thenReturn(Optional.of(existingCoach));
        when(coachRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndIdNot(anyString(), anyString(), anyLong()))
                .thenReturn(false);
        coachService.update(1L, new CoachCreateUpdateRequest("Petr", "Sidorov"));
        coachService.delete(1L);

        Athlete athlete = new Athlete("Alex", "Smirnov", null);
        when(athleteRepository.save(any(Athlete.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(athleteRepository.findById(2L)).thenReturn(Optional.of(athlete));
        when(athleteRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(anyString(), anyString())).thenReturn(false);
        when(athleteRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndIdNot(anyString(), anyString(), anyLong()))
                .thenReturn(false);
        athleteService.create(new AthleteCreateUpdateRequest(" Alex ", " Smirnov ", null));
        athleteService.update(2L, new AthleteCreateUpdateRequest("Alex", "Smirnov", null));
        athleteService.delete(2L);

        Exercise exercise = new Exercise("Row");
        when(exerciseRepository.findById(3L)).thenReturn(Optional.of(exercise));
        when(exerciseRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(exerciseRepository.existsByNameIgnoreCaseAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(exerciseRepository.save(any(Exercise.class))).thenAnswer(invocation -> invocation.getArgument(0));
        exerciseService.create(new ExerciseCreateUpdateRequest(" Row "));
        exerciseService.update(3L, new ExerciseCreateUpdateRequest(" Pull Up "));
        exerciseService.delete(3L);

        TrainingProgram program = new TrainingProgram("Mass");
        when(trainingProgramRepository.findById(4L)).thenReturn(Optional.of(program));
        when(trainingProgramRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(trainingProgramRepository.existsByNameIgnoreCaseAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(trainingProgramRepository.save(any(TrainingProgram.class))).thenAnswer(invocation -> invocation.getArgument(0));
        trainingProgramService.create(new TrainingProgramCreateUpdateRequest(" Mass "));
        trainingProgramService.update(4L, new TrainingProgramCreateUpdateRequest("Cutting"));
        trainingProgramService.delete(4L);
    }
}
