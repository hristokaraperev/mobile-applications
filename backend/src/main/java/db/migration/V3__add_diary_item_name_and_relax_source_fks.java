package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Adds a write-time {@code item_name} snapshot to diary entries and relaxes the
 * {@code food_id}/{@code recipe_id} foreign keys to {@code ON DELETE SET NULL}, so a
 * diary entry (and its snapshotted name and nutrition) survives deletion of its source
 * food or recipe.
 *
 * <p>This is a Java migration rather than plain SQL because the V1 foreign keys were
 * created inline without explicit names, and the generated names differ between Postgres
 * (production) and H2 in PostgreSQL mode (tests). The constraint names are therefore looked
 * up from {@code information_schema} at migration time before being recreated.
 */
public class V3__add_diary_item_name_and_relax_source_fks extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE diary_entries ADD COLUMN item_name VARCHAR(255)");
        }

        relaxForeignKey(connection, "food_id", "foods");
        relaxForeignKey(connection, "recipe_id", "recipes");
    }

    /**
     * Drops the existing foreign key on {@code diary_entries.column} (whatever its generated
     * name) and recreates it referencing {@code referencedTable(id)} with {@code ON DELETE SET NULL}.
     *
     * @param connection      the migration connection.
     * @param column          the referencing column on {@code diary_entries}.
     * @param referencedTable the table {@code column} references by its {@code id}.
     */
    private void relaxForeignKey(Connection connection, String column, String referencedTable) throws Exception {
        for (String name : foreignKeyNames(connection, column)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE diary_entries DROP CONSTRAINT \"" + name + "\"");
            }
        }

        String newName = "fk_diary_" + column;
        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    "ALTER TABLE diary_entries ADD CONSTRAINT " + newName +
                            " FOREIGN KEY (" + column + ") REFERENCES " + referencedTable +
                            " (id) ON DELETE SET NULL");
        }
    }

    /**
     * Returns the names of the foreign-key constraints on {@code diary_entries.column}, read
     * from {@code information_schema} so the lookup works on both Postgres and H2.
     */
    private List<String> foreignKeyNames(Connection connection, String column) throws Exception {
        String sql =
                "SELECT tc.constraint_name " +
                        "FROM information_schema.table_constraints tc " +
                        "JOIN information_schema.key_column_usage kcu " +
                        "  ON tc.constraint_name = kcu.constraint_name " +
                        " AND tc.table_name = kcu.table_name " +
                        "WHERE tc.constraint_type = 'FOREIGN KEY' " +
                        "  AND LOWER(tc.table_name) = 'diary_entries' " +
                        "  AND LOWER(kcu.column_name) = ?";

        List<String> names = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, column);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString(1));
                }
            }
        }

        return names;
    }
}
