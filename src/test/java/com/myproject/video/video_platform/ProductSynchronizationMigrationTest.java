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

class ProductSynchronizationMigrationTest {
    @Test
    void availabilityCascadesAndMediaConstraintsAreIndependentOfProductInheritance() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:product_sync_migration;MODE=PostgreSQL;DATABASE_TO_UPPER=false", "sa", "")) {
            connection.createStatement().execute("CREATE TABLE consultation_products (id UUID PRIMARY KEY)");
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("db/changelog/scripts/39-create-consultation-availability.sql"));
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("db/changelog/scripts/40-create-product-media.sql"));
            UUID productId = UUID.randomUUID(); UUID dayId = UUID.randomUUID();
            connection.createStatement().executeUpdate("INSERT INTO consultation_products VALUES ('" + productId + "')");
            connection.createStatement().executeUpdate("INSERT INTO consultation_availability_days(id, consultation_product_id, weekday, enabled) VALUES ('"
                    + dayId + "','" + productId + "','MONDAY',TRUE)");
            connection.createStatement().executeUpdate("INSERT INTO consultation_availability_windows(id, availability_day_id, start_time, end_time, position) VALUES ('"
                    + UUID.randomUUID() + "','" + dayId + "','09:00','17:00',0)");
            assertThrows(SQLException.class, () -> connection.createStatement().executeUpdate(
                    "INSERT INTO consultation_availability_days(id, consultation_product_id, weekday, enabled) VALUES ('"
                            + UUID.randomUUID() + "','" + productId + "','MONDAY',FALSE)"));
            connection.createStatement().execute("DELETE FROM consultation_products WHERE id='" + productId + "'");
            assertEquals(0, count(connection, "consultation_availability_days"));
            assertEquals(0, count(connection, "consultation_availability_windows"));

            UUID unknownProduct = UUID.randomUUID();
            connection.createStatement().executeUpdate("INSERT INTO product_media(id,product_id,kind,object_key,cdn_url,file_name,mime_type,file_size,gallery_position,status) VALUES ('"
                    + UUID.randomUUID() + "','" + unknownProduct + "','GALLERY_IMAGE','key','https://cdn/key','x.png','image/png',1,0,'READY')");
            assertEquals(1, count(connection, "product_media"));
        }
    }

    private static int count(Connection connection, String table) throws SQLException {
        try (var result = connection.createStatement().executeQuery("SELECT COUNT(*) FROM " + table)) {
            result.next(); return result.getInt(1);
        }
    }
}
