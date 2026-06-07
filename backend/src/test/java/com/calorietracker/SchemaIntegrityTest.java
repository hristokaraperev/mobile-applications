package com.calorietracker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies the constraints and columns the migration is supposed to create, by
 * exercising them: a duplicate email is rejected, a bad enum value is rejected,
 * the {@code foods} table carries its full nutrition column set, and the barcode
 * lookup column is indexed.
 */
@Transactional
class SchemaIntegrityTest extends AbstractIntegrationTest {

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    DataSource dataSource;

    @Test
    void emailIsUnique() {
        jdbc.update("INSERT INTO users (email, password_hash) VALUES ('dup@example.com', 'x')");

        assertThatThrownBy(() ->
                jdbc.update("INSERT INTO users (email, password_hash) VALUES ('dup@example.com', 'y')"))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void foodTypeCheckConstraintRejectsUnknownValue() {
        assertThatThrownBy(() ->
                jdbc.update("INSERT INTO foods (name, type, source, energy_kcal) "
                        + "VALUES ('Mystery', 'BOGUS', 'USER', 100)"))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void foodsHasFullNutritionColumnSet() {
        List<String> columns = jdbc.queryForList(
                "SELECT LOWER(column_name) FROM information_schema.columns "
                        + "WHERE LOWER(table_name) = 'foods'",
                String.class);

        assertThat(columns).contains(
                "id", "name", "brand", "barcode", "type", "source",
                "energy_kcal", "protein_g", "carbs_g", "sugars_g", "fat_g",
                "sat_fat_g", "fiber_g", "salt_g", "serving_size_g",
                "owner_user_id", "updated_at");
    }

    @Test
    void barcodeColumnIsIndexed() throws Exception {
        assertThat(isColumnIndexed("foods", "barcode")).isTrue();
    }

    private boolean isColumnIndexed(String table, String column) throws Exception {
        try (Connection connection = dataSource.getConnection();
             ResultSet rs = connection.getMetaData()
                     .getIndexInfo(null, null, table, false, false)) {
            while (rs.next()) {
                String indexed = rs.getString("COLUMN_NAME");
                if (column.equalsIgnoreCase(indexed)) {
                    return true;
                }
            }
        }
        return false;
    }
}
