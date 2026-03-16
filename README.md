# Training Project (Spring Boot + JPA + PostgreSQL)

Проект демонстрирует:

1. Подключение реляционной БД PostgreSQL + Spring Data JPA.
2. Модель из 5 сущностей со связями OneToMany и ManyToMany.
3. CRUD операции для `Workout`.
4. Осознанное применение `CascadeType` и `FetchType` (используется только LAZY).
5. Проблему N+1 и её решение через `@EntityGraph`.
6. Сценарий частичного сохранения без `@Transactional` и откат с `@Transactional`.
7. Сложный `GET`-запрос с фильтрацией по вложенной сущности через `@Query` (JPQL).
8. Аналогичный запрос через `native query`.
9. Пагинацию результатов с использованием `Pageable`.
10. In-memory индекс на базе `HashMap<K, V>` для ранее запрошенных данных с составным ключом.
11. Корректную работу составного ключа за счёт правильной реализации `equals()` и `hashCode()`.
12. Инвалидацию in-memory индекса при изменении данных.

## Сущности и связи

- `Coach`
- `Athlete`
- `TrainingProgram`
- `Workout`
- `Exercise`

### Связи
- `Coach (1) -> (N) Athlete` (OneToMany)
- `Athlete (1) -> (N) Workout` (OneToMany)
- `TrainingProgram (1) -> (N) Workout` (OneToMany)
- `Workout (N) <-> (N) Exercise` (ManyToMany)

## ER-диаграмма (PK/FK)

```mermaid
erDiagram
    coaches ||--o{ athletes : "coach_id"
    athletes ||--o{ workouts : "athlete_id"
    programs ||--o{ workouts : "program_id"
    workouts ||--o{ workout_exercises : "workout_id"
    exercises ||--o{ workout_exercises : "exercise_id"

    coaches {
        bigint id PK
        varchar first_name
        varchar last_name
    }

    athletes {
        bigint id PK
        varchar first_name
        varchar last_name
        bigint coach_id FK
    }

    programs {
        bigint id PK
        varchar name
    }

    workouts {
        bigint id PK
        varchar title
        int duration_minutes
        timestamp scheduled_at
        bigint athlete_id FK
        bigint program_id FK
    }

    exercises {
        bigint id PK
        varchar name
    }

    workout_exercises {
        bigint workout_id FK
        bigint exercise_id FK
    }
   ```

## Почему такие CascadeType и FetchType

- Везде выставлен `FetchType.LAZY`, чтобы не загружать граф объектов без явной необходимости.
- `Coach -> Athlete` и `TrainingProgram -> Workout`: `CascadeType.PERSIST`, чтобы при создании родителя можно было сохранить дочерние записи, но не удалять их каскадно случайно.
- `Athlete -> Workout`: `CascadeType.ALL + orphanRemoval=true` — тренировки являются частью жизненного цикла спортсмена в данной модели.
- На `ManyToMany` каскад не задан, чтобы не удалять/не изменять упражнения при удалении тренировки.

## N+1 проблема и решение

- Проблемный метод: `WorkoutService#getAllWorkouts()` использует `workoutRepository.findAll()` и затем обращается к LAZY-полям в маппере.
- Оптимизированный метод: `WorkoutService#getAllWorkoutsOptimized()` использует `WorkoutRepository#findAllWithDetails()` с `@EntityGraph(attributePaths = {"athlete", "program", "exercises"})`.

Эндпоинты:

- `GET /api/workouts` — пример с потенциальной N+1.
- `GET /api/workouts/optimized` — решение через `@EntityGraph`.

## CRUD для Workout

- `POST /api/workouts`
- `GET /api/workouts/{id}`
- `GET /api/workouts`
- `PUT /api/workouts/{id}`
- `DELETE /api/workouts/{id}`

## Примеры запросов 3 лаба

> База URL: `http://localhost:8080`

### 1) JPQL + фильтрация по вложенной сущности + Pageable

```bash
curl -G "http://localhost:8080/api/workouts/search/jpql" \
  --data-urlencode "coachId=1" \
  --data-urlencode "programId=1" \
  --data-urlencode "page=0" \
  --data-urlencode "size=5" \
  --data-urlencode "sort=scheduledAt,desc"
```

### 2) Тот же поиск через native query + Pageable

```bash
curl -G "http://localhost:8080/api/workouts/search/native" \
  --data-urlencode "coachId=1" \
  --data-urlencode "programId=1" \
  --data-urlencode "page=0" \
  --data-urlencode "size=5" \
  --data-urlencode "sort=scheduledAt,desc"
```

### 3) Демонстрация кэша (in-memory index)

Повтори один и тот же запрос 2 раза подряд — второй раз должен быть `HIT` в логах:

```bash
curl -G "http://localhost:8080/api/workouts/search/jpql" \
  --data-urlencode "coachId=1" \
  --data-urlencode "programId=1" \
  --data-urlencode "page=0" \
  --data-urlencode "size=5"
```

### 4) Демонстрация инвалидации индекса

1. Сначала прогрей кэш (запрос выше).
2. Затем измени данные (любой `POST/PUT/DELETE` на `/api/workouts`).
3. Снова вызови тот же поиск — в логах снова будет `MISS`.

Пример создания новой тренировки:

```bash
curl -X POST "http://localhost:8080/api/workouts" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Demo cache invalidation",
    "type": "Strength",
    "durationMinutes": 55,
    "scheduledAt": "2026-03-05T10:22:25",
    "athleteId": 1,
    "programId": 1,
    "exerciseIds": [1, 2]
  }'
```

## Транзакции: с и без @Transactional

Для демонстрации реализованы два endpoint:

- `POST /api/workouts/with_exercises_without_tx`
- вызывает метод **без** `@Transactional`.
- сначала сохраняет Coach/Athlete/Program, затем пытается сохранить некорректную Workout (`title=null`, NOT NULL constraint).
- результат: часть данных уже в БД (частичное сохранение).

- `POST /api/workouts/with_exercises`
- вызывает метод **с** `@Transactional`.
- при той же ошибке откатывается вся операция.
- результат: полное откатывание изменений в рамках метода.

## запрос для проверки @Transactional
{
"title": "demo1",
"type": "demo1",
"durationMinutes": 70,
"scheduledAt": "2026-03-05T10:22:25",
"athleteId": null,
"programId": 1,
"exerciseIds": [1]
}

## Запуск PostgreSQL

```bash
docker compose up -d
```

Контейнер поднимает БД с параметрами:
- DB: `training_db`
- user: `postgres`
- password: `postgres`
- port: `5432`

## Запуск приложения

```bash
./mvnw spring-boot:run
```

Приложение подключается к PostgreSQL через:
- `jdbc:postgresql://localhost:5432/trainingdb`
