package com.calorietracker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the Flyway baseline migration creates the full schema. Reads the
 * live database catalog rather than the migration text, so it proves the SQL
 * actually applied.
 */
class FlywayMigrationTest extends AbstractIntegrationTest {

    @Autowired
    JdbcTemplate jdbc;

    private List<String> tableNames() {
        return jdbc.queryForList(
                "SELECT LOWER(table_name) FROM information_schema.tables "
                        + "WHERE table_schema IN ('public', 'PUBLIC')",
                String.class);
    }

    @Test
    void createsAllFiveTables() {
        assertThat(tableNames()).contains(
                "users", "foods", "recipes", "recipe_ingredients", "diary_entries");
    }
}
