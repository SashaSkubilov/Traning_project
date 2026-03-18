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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Native PostgreSQL search implementation with explicit column mapping for sorting.
 */
@Repository
public class WorkoutNativeSearchRepositoryImpl implements WorkoutNativeSearchRepository {

    private static final Map<String, String> SORT_COLUMNS = createSortColumns();

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Workout> findByFiltersNative(
                                             final Long coachId,
                                             final Long programId,
                                             final Pageable pageable) {
        String fromClause = """
              
                join athletes a on w.athlete_id = a.id
                join coaches c on a.coach_id = c.id
                join programs p on w.program_id = p.id
                where (:coachId is null or c.id = :coachId)
                  and (:programId is null or p.id = :programId)
                """;

        String sql = "select w.*\n" + fromClause + buildOrderBy(pageable.getSort());
        Query query = entityManager.createNativeQuery(sql, Workout.class);
        bindParameters(query, coachId, programId);

        if (pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        @SuppressWarnings("unchecked")
        List<Workout> content = query.getResultList();

        Query countQuery = entityManager.createNativeQuery("select count(*)\n" + fromClause);
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

    private String buildOrderBy(final Sort sort) {
        if (sort.isUnsorted()) {
            return "";
        }

        StringBuilder orderBy = new StringBuilder(" order by ");
        boolean first = true;
        for (Sort.Order order : sort) {
            String column = SORT_COLUMNS.get(order.getProperty());
            if (column == null) {
                continue;
            }

            if (!first) {
                orderBy.append(", ");
            }

            orderBy.append(column)
                    .append(order.isDescending() ? " desc" : " asc");
            first = false;
        }

        return first ? "" : orderBy.toString();
    }

    private static Map<String, String> createSortColumns() {
        Map<String, String> sortColumns = new HashMap<>();
        sortColumns.put("id", "w.id");
        sortColumns.put("title", "w.title");
        sortColumns.put("durationMinutes", "w.duration_minutes");
        sortColumns.put("scheduledAt", "w.scheduled_at");
        return sortColumns;
    }
}
