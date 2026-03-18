package com.example.training_project.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Migrates the legacy workouts.type column from bytea to text for PostgreSQL.
 */
@Component
public class WorkoutTypeColumnMigration implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(WorkoutTypeColumnMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public WorkoutTypeColumnMigration(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(final ApplicationArguments args) {
        String columnType = jdbcTemplate.query(
                """
                select data_type
                from information_schema.columns
                where table_schema = current_schema()
                  and table_name = 'workouts'
                  and column_name = 'type'
                """,
                rs -> rs.next() ? rs.getString("data_type") : null
        );

        if (columnType == null) {
            LOG.debug("Skipping workouts.type migration because the column does not exist yet.");
            return;
        }

        if (!"bytea".equalsIgnoreCase(columnType)) {
            LOG.debug("Skipping workouts.type migration because the column is already {}.", columnType);
            return;
        }

        jdbcTemplate.execute("""
                alter table workouts
                alter column type type text
                using encode(type, 'escape')
                """);

        LOG.info("Migrated workouts.type column from bytea to text.");
    }
}