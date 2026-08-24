package com.myproject.video.video_platform;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PresentationMigrationTest {
    @Test
    void migrationsCreateProfileAndPresentationConstraintsWithoutProductForeignKeys() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:presentation_migration;MODE=PostgreSQL;DATABASE_TO_UPPER=false", "sa", "")) {
            connection.createStatement().execute("CREATE TABLE users (user_id UUID PRIMARY KEY)");
            connection.createStatement().execute("INSERT INTO users VALUES ('00000000-0000-0000-0000-000000000010')");
            for (int migration = 36; migration <= 38; migration++) {
                String name = switch (migration) {
                    case 36 -> "36-add-public-email.sql";
                    case 37 -> "37-create-storefront-config.sql";
                    default -> "38-create-product-landing-page-config.sql";
                };
                ScriptUtils.executeSqlScript(connection, new ClassPathResource("db/changelog/scripts/" + name));
            }

            UUID storefrontId = UUID.randomUUID();
            UUID unknownProduct = UUID.randomUUID();
            connection.createStatement().executeUpdate("""
                    INSERT INTO storefront_configs(id, creator_id) VALUES ('%s','00000000-0000-0000-0000-000000000010')
                    """.formatted(storefrontId));
            connection.createStatement().executeUpdate("""
                    INSERT INTO storefront_product_order(storefront_config_id, product_id, position) VALUES ('%s','%s',0)
                    """.formatted(storefrontId, unknownProduct));
            assertThrows(SQLException.class, () -> connection.createStatement().executeUpdate("""
                    INSERT INTO storefront_product_order(storefront_config_id, product_id, position) VALUES ('%s','%s',1)
                    """.formatted(storefrontId, unknownProduct)));

            UUID landingId = UUID.randomUUID();
            connection.createStatement().executeUpdate("""
                    INSERT INTO product_landing_page_configs(id, product_id) VALUES ('%s','%s')
                    """.formatted(landingId, unknownProduct));
            connection.createStatement().executeUpdate("""
                    INSERT INTO product_landing_section_order(config_id, section_id, position)
                    VALUES ('%s','ABOUT',0),('%s','CONTENTS',1),('%s','CREATOR',2)
                    """.formatted(landingId, landingId, landingId));
            connection.createStatement().execute("DELETE FROM product_landing_page_configs WHERE id = '" + landingId + "'");
            assertEquals(0, count(connection, "product_landing_section_order"));

            connection.createStatement().execute("DELETE FROM storefront_configs WHERE id = '" + storefrontId + "'");
            assertEquals(0, count(connection, "storefront_product_order"));
        }
    }

    private static int count(Connection connection, String table) throws SQLException {
        try (var result = connection.createStatement().executeQuery("SELECT COUNT(*) FROM " + table)) {
            result.next();
            return result.getInt(1);
        }
    }
}
