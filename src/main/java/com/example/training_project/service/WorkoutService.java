package com.example.training_project.service;

import com.example.training_project.dto.WorkoutCreateUpdateRequest;
import com.example.training_project.dto.WorkoutDto;
import com.example.training_project.dto.WorkoutWithExercisesRequest;
import com.example.training_project.entity.Athlete;
import com.example.training_project.entity.Exercise;
import com.example.training_project.entity.TrainingProgram;
import com.example.training_project.entity.Workout;
import com.example.training_project.mapper.WorkoutMapper;
import com.example.training_project.repository.AthleteRepository;
import com.example.training_project.repository.CoachRepository;
import com.example.training_project.repository.ExerciseRepository;
import com.example.training_project.repository.TrainingProgramRepository;
import com.example.training_project.repository.WorkoutRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Service for managing workouts and demonstrating transactional behavior.
 */
@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;

    private final AthleteRepository athleteRepository;

    private final TrainingProgramRepository trainingProgramRepository;

    private final ExerciseRepository exerciseRepository;

    private final CoachRepository coachRepository;

    private final WorkoutMapper workoutMapper;

    public WorkoutService(
            final WorkoutRepository workoutRepository,
            final AthleteRepository athleteRepository,
            final TrainingProgramRepository trainingProgramRepository,
            final ExerciseRepository exerciseRepository,
            final CoachRepository coachRepository,
            final WorkoutMapper workoutMapper
    ) {
        this.workoutRepository = workoutRepository;
        this.athleteRepository = athleteRepository;
        this.trainingProgramRepository = trainingProgramRepository;
        this.exerciseRepository = exerciseRepository;
        this.coachRepository = coachRepository;
        this.workoutMapper = workoutMapper;
    }

    @Transactional(readOnly = true)
    public List<WorkoutDto> getAllWorkouts() {
        return workoutRepository.findAll().stream()
                .map(workoutMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutDto> getWorkouts(final String type) {
        return workoutRepository.findAll().stream()
                .filter(w -> type == null || type.equalsIgnoreCase(w.getType()))
                .map(workoutMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutDto> getAllWorkoutsOptimized() {
        return workoutRepository.findAllWithDetails().stream()
                .map(workoutMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkoutDto getWorkoutById(final Long id) {
        return workoutRepository.findById(id)
                .map(workoutMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Workout not found: " + id));
    }

    @Transactional
    public WorkoutDto createWorkout(final WorkoutCreateUpdateRequest request) {
        Workout workout = buildWorkoutEntity(new Workout(), request);
        return workoutMapper.toDto(workoutRepository.save(workout));
    }

    @Transactional
    public WorkoutDto updateWorkout(final Long id, final WorkoutCreateUpdateRequest request) {

        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Workout not found: " + id));
        buildWorkoutEntity(workout, request);
        return workoutMapper.toDto(workoutRepository.save(workout));
    }

    @Transactional
    public WorkoutDto addWorkoutWithExercises(final WorkoutWithExercisesRequest request) {
        return addWorkoutWithExercisesInternal(request, true);
    }

    public WorkoutDto addWorkoutWithExercisesWithoutTransaction(final WorkoutWithExercisesRequest request) {
        return addWorkoutWithExercisesInternal(request, true);
    }

    @Transactional
    public void deleteWorkout(final Long id) {
        workoutRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public String getPersistedCounts() {
        return "coaches=" + coachRepository.count()
                + ", athletes=" + athleteRepository.count()
                + ", programs=" + trainingProgramRepository.count()
                + ", exercises=" + exerciseRepository.count()
                + ", workouts=" + workoutRepository.count();
    }

    private WorkoutDto addWorkoutWithExercisesInternal(final WorkoutWithExercisesRequest request,
                                                       final boolean simulateFailure) {
        Workout workout = buildWorkoutWithExercisesEntity(request);
        if (simulateFailure && "FAIL".equalsIgnoreCase(request.title())) {
            throw new RuntimeException("Simulated failure after saving exercises");
        }
        return workoutMapper.toDto(workoutRepository.save(workout));
    }

    private Workout buildWorkoutWithExercisesEntity(final WorkoutWithExercisesRequest request) {
        Workout workout = new Workout();
        workout.setTitle(request.title());
        workout.setType(request.type());
        workout.setDurationMinutes(request.durationMinutes());
        workout.setScheduledAt(request.scheduledAt());
        workout.setAthlete(getAthleteById(request.athleteId()));
        workout.setProgram(getProgramById(request.programId()));

        saveExercises(request.exerciseNames()).forEach(workout.getExercises()::add);
        return workout;
    }

    private List<Exercise> saveExercises(final List<String> exerciseNames) {
        if (exerciseNames == null) {
            throw new IllegalArgumentException("exerciseNames must not be null");
        }

        List<String> normalizedNames = exerciseNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .toList();

        if (normalizedNames.isEmpty()) {
            throw new IllegalArgumentException("exerciseNames must contain at least one value");
        }

        return normalizedNames.stream()
                .map(Exercise::new)
                .map(exerciseRepository::save)
                .toList();
    }

    private Athlete getAthleteById(final Long athleteId) {
        return athleteRepository.findById(athleteId)
                .orElseThrow(() -> new EntityNotFoundException("Athlete not found: " + athleteId));
    }

    private TrainingProgram getProgramById(final Long programId) {
        return trainingProgramRepository.findById(programId)
                .orElseThrow(() -> new EntityNotFoundException("Program not found: " + programId));
    }

    private Workout buildWorkoutEntity(final Workout workout, final WorkoutCreateUpdateRequest request) {
        Athlete athlete = athleteRepository.findById(request.athleteId())
                .orElseThrow(() -> new EntityNotFoundException("Athlete not found: " + request.athleteId()));
        TrainingProgram program = trainingProgramRepository.findById(request.programId())
                .orElseThrow(() -> new EntityNotFoundException("Program not found: " + request.programId()));

        Set<Exercise> exercises = new HashSet<>(exerciseRepository.findAllById(request.exerciseIds()));

        workout.setTitle(request.title());
        workout.setType(request.type());
        workout.setDurationMinutes(request.durationMinutes());
        workout.setScheduledAt(request.scheduledAt());
        workout.setAthlete(athlete);
        workout.setProgram(program);

        if (workout.getExercises() != null) {
            workout.getExercises().clear();
            workout.getExercises().addAll(exercises);
        }

        return workout;
    }
}
