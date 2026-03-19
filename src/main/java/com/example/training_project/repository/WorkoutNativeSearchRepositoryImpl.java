package com.example.training_project.repository;

import com.example.training_project.entity.Workout;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * * Native PostgreSQL search implementation with safe sort mapping.
 */
@Repository
public class WorkoutNativeSearchRepositoryImpl implements WorkoutNativeSearchRepository {

    private static final int MAX_SORT_FIELDS = 4;

    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "id", "w.id",
            "title", "w.title",
            "durationMinutes", "w.duration_minutes",
            "scheduledAt", "w.scheduled_at"
    );

    private static final String FROM_CLAUSE = """
            from workouts w
            join athletes a on w.athlete_id = a.id
            join coaches c on a.coach_id = c.id
            join programs p on w.program_id = p.id
            where (:coachId is null or c.id = :coachId)
              and (:programId is null or p.id = :programId)
            """;

    private static final String ORDER_BY_CLAUSE = buildOrderByClause();

    private static final String CONTENT_QUERY = "select w.*\n" + FROM_CLAUSE + ORDER_BY_CLAUSE;

    private static final String COUNT_QUERY = "select count(*)\n" + FROM_CLAUSE;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Workout> findByFiltersNative(final Long coachId,
                                             final Long programId,
                                             final Pageable pageable) {
        Query query = entityManager.createNativeQuery(CONTENT_QUERY, Workout.class);
        bindParameters(query, coachId, programId);
        bindSortParameters(query, pageable.getSort());

        if (pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        @SuppressWarnings("unchecked")
        List<Workout> content = query.getResultList();

        Query countQuery = entityManager.createNativeQuery(COUNT_QUERY);
        bindParameters(countQuery, coachId, programId);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(content, pageable, total);
    }

    private void bindParameters(final Query query,
                                final Long coachId,
                                final Long programId) {
        query.setParameter("coachId", coachId);
        query.setParameter("programId", programId);
    }

    private void bindSortParameters(final Query query,
                                    final Sort sort) {
        List<SortBinding> sortBindings = normalizeSort(sort);
        for (int i = 0; i < MAX_SORT_FIELDS; i++) {
            SortBinding binding = i < sortBindings.size() ? sortBindings.get(i) : SortBinding.unsorted();
            query.setParameter("sort" + i + "Property", binding.property());
            query.setParameter("sort" + i + "Direction", binding.direction());
        }
    }

    private List<SortBinding> normalizeSort(final Sort sort) {
        List<SortBinding> sortBindings = new ArrayList<>();
        for (Sort.Order order : sort) {
            if (!SORT_COLUMNS.containsKey(order.getProperty())) {
                continue;
            }
            sortBindings.add(new SortBinding(order.getProperty(), order.isDescending() ? "desc" : "asc"));
            if (sortBindings.size() == MAX_SORT_FIELDS) {
                break;
            }
        }
        return sortBindings;
    }

    private static String buildOrderByClause() {
        StringBuilder orderBy = new StringBuilder("order by\n");
        boolean firstClause = true;
        for (int index = 0; index < MAX_SORT_FIELDS; index++) {
            for (Map.Entry<String, String> entry : SORT_COLUMNS.entrySet()) {
                if (!firstClause) {
                    orderBy.append(",\n");
                }
                appendOrderClause(orderBy, index, entry.getKey(), entry.getValue(), "asc");
                orderBy.append(",\n");
                appendOrderClause(orderBy, index, entry.getKey(), entry.getValue(), "desc");
                firstClause = false;
            }
        }
        orderBy.append(",\n  w.id asc\n");
        return orderBy.toString();
    }

    private static void appendOrderClause(final StringBuilder orderBy,
                                          final int index,
                                          final String property,
                                          final String column,
                                          final String direction) {
        orderBy.append("  case when :sort")
                .append(index)
                .append("Property = '")
                .append(property)
                .append("' and :sort")
                .append(index)
                .append("Direction = '")
                .append(direction)
                .append("' then ")
                .append(column)
                .append(" end ")
                .append(direction);
    }

        private record SortBinding(String property, String direction) {

            private static SortBinding unsorted() {
                return new SortBinding("__unsorted__", "asc");
            }
    }
}
