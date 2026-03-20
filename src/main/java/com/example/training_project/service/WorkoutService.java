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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;


@Service
public class WorkoutService {

    private static final Logger LOG = LoggerFactory.getLogger(WorkoutService.class);

    private final WorkoutRepository workoutRepository;

    private final AthleteRepository athleteRepository;

    private final TrainingProgramRepository trainingProgramRepository;

    private final ExerciseRepository exerciseRepository;

    private final CoachRepository coachRepository;

    private final WorkoutMapper workoutMapper;

    private final Map<WorkoutFilterKey, Page<WorkoutDto>> workoutIndex = new ConcurrentHashMap<>();

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
    public Page<WorkoutDto> searchWorkoutsJpql(final String type,
                                               final String coachName,
                                               final String programName,
                                               final Pageable pageable) {
        WorkoutFilterKey key = WorkoutFilterKey.forQuery(
                WorkoutFilterKey.QueryType.JPQL,
                type,
                coachName,
                programName,
                pageable
        );

        return getOrLoadFromIndex(
                key,
                () -> loadJpqlSearchPage(type, coachName, programName, pageable)
        );
    }

    private Page<WorkoutDto> loadJpqlSearchPage(final String type,
                                                final String coachName,
                                                final String programName,
                                                final Pageable pageable) {
        if (type == null || type.isBlank()) {
            return workoutRepository.findByFiltersJpql(type, coachName, programName, pageable)
                    .map(workoutMapper::toDto);
        }

        Pageable filterPageable = PageRequest.of(0, Integer.MAX_VALUE, pageable.getSort());
        List<WorkoutDto> filtered = workoutRepository.findByFiltersJpql(null, coachName, programName, filterPageable)
                .stream()
                .filter(workout -> type.equalsIgnoreCase(workout.getType()))
                .map(workoutMapper::toDto)
                .toList();

        int start = Math.toIntExact(Math.min(pageable.getOffset(), filtered.size()));
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<WorkoutDto> pageContent = filtered.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, filtered.size());
    }

    @Transactional(readOnly = true)
    public Page<WorkoutDto> searchWorkoutsNative(final String coachName,
                                                 final String programName,
                                                 final Pageable pageable) {
        WorkoutFilterKey key = WorkoutFilterKey.forQuery(
                WorkoutFilterKey.QueryType.NATIVE,
                null,
                coachName,
                programName,
                pageable
        );

        return getOrLoadFromIndex(
                key,
                () -> preloadWorkoutDetails(workoutRepository.findByFiltersNative(coachName, programName, pageable))
                        .map(workoutMapper::toDto)
        );
    }

    private Page<Workout> preloadWorkoutDetails(final Page<Workout> page) {
        List<Long> workoutIds = page.getContent().stream()
                .map(Workout::getId)
                .toList();

        if (workoutIds.isEmpty()) {
            return page;
        }

        Map<Long, Workout> workoutsById = workoutRepository.findAllWithDetailsByIdIn(workoutIds).stream()
                .collect(java.util.stream.Collectors.toMap(Workout::getId, workout -> workout));

        List<Workout> hydratedContent = workoutIds.stream()
                .map(workoutsById::get)
                .filter(Objects::nonNull)
                .toList();

        return new org.springframework.data.domain.PageImpl<>(
                hydratedContent,
                page.getPageable(),
                page.getTotalElements());
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
        WorkoutDto result = workoutMapper.toDto(workoutRepository.save(workout));
        invalidateWorkoutIndex();
        return result;
    }

    @Transactional
    public WorkoutDto updateWorkout(final Long id, final WorkoutCreateUpdateRequest request) {

        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Workout not found: " + id));
        buildWorkoutEntity(workout, request);
        WorkoutDto result = workoutMapper.toDto(workoutRepository.save(workout));
        invalidateWorkoutIndex();
        return result;
    }

    @Transactional
    public WorkoutDto addWorkoutWithExercises(final WorkoutWithExercisesRequest request) {
        WorkoutDto result = addWorkoutWithExercisesInternal(request, true);
        invalidateWorkoutIndex();
        return result;
    }


    @Transactional
    public void deleteWorkout(final Long id) {
        workoutRepository.deleteById(id);
        invalidateWorkoutIndex();
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

    private void invalidateWorkoutIndex() {
        int previousSize = workoutIndex.size();
        workoutIndex.clear();
        LOG.info("Workout index invalidated. Entries removed: {}", previousSize);
    }

    private Page<WorkoutDto> getOrLoadFromIndex(final WorkoutFilterKey key,
                                                final Supplier<Page<WorkoutDto>> loader) {
        Page<WorkoutDto> cached = workoutIndex.get(key);
        if (cached != null) {
            LOG.info("Workout index HIT for key: {}", key.toLogSafeString());
            return cached;
        }

        LOG.info("Workout index MISS for key: {}", key.toLogSafeString());
        Page<WorkoutDto> loaded = loader.get();
        workoutIndex.put(key, loaded);
        return loaded;
    }

    private static final class WorkoutFilterKey {

        enum QueryType {
            JPQL,
            NATIVE
        }

        private final QueryType queryType;

        private final String type;

        private final String coachName;

        private final String programName;

        private final int pageNumber;

        private final int pageSize;

        private final String sort;

        private WorkoutFilterKey(final QueryType queryType,
                                 final String type,
                                 final String coachName,
                                 final String programName,
                                 final int pageNumber,
                                 final int pageSize,
                                 final String sort) {
            this.queryType = queryType;
            this.type = type;
            this.coachName = coachName;
            this.programName = programName;
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
            this.sort = sort;
        }

        static WorkoutFilterKey forQuery(final QueryType queryType,
                                         final String type,
                                         final String coachName,
                                         final String programName,
                                         final Pageable pageable) {
            String sort = pageable.getSort().isUnsorted() ? "" : pageable.getSort().toString();
            return new WorkoutFilterKey(
                    queryType,
                    type,
                    coachName,
                    programName,
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    sort
            );
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            WorkoutFilterKey that = (WorkoutFilterKey) o;
            return pageNumber == that.pageNumber
                    && pageSize == that.pageSize
                    && queryType == that.queryType
                    && Objects.equals(type, that.type)
                    && Objects.equals(coachName, that.coachName)
                    && Objects.equals(programName, that.programName)
                    && Objects.equals(sort, that.sort);
        }

        @Override
        public int hashCode() {
            return Objects.hash(queryType, type, coachName, programName, pageNumber, pageSize, sort);
        }

        String toLogSafeString() {
            return "WorkoutFilterKey{"
                    + "queryType=" + queryType
                    + ", type='" + sanitizeForLog(type) + '\''
                    + ", coachName='" + sanitizeForLog(coachName) + '\''
                    + ", programName='" + sanitizeForLog(programName) + '\''
                    + ", pageNumber=" + pageNumber
                    + ", pageSize=" + pageSize
                    + ", sort='" + sanitizeForLog(sort) + '\''
                    + '}';
        }

        private static String sanitizeForLog(final String value) {
            if (value == null) {
                return null;
            }
            return value
                    .replace("\r", "_")
                    .replace("\n", "_");
        }
    }
}
