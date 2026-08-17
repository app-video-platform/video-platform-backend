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

class MembershipMigrationTest {

    @Test
    void migrationBackfillsPricingAndEnforcesMembershipShapesAndCascades() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:membership_migration;MODE=PostgreSQL;DATABASE_TO_UPPER=false", "sa", "")) {
            ScriptUtils.executeSqlScript(connection, new org.springframework.core.io.ByteArrayResource("""
                    CREATE TABLE users (user_id UUID PRIMARY KEY);
                    CREATE TABLE course_products (id UUID PRIMARY KEY);
                    CREATE TABLE download_products (id UUID PRIMARY KEY);
                    CREATE TABLE consultation_products (id UUID PRIMARY KEY);
                    INSERT INTO course_products(id) VALUES ('00000000-0000-0000-0000-000000000001');
                    INSERT INTO download_products(id) VALUES ('00000000-0000-0000-0000-000000000002');
                    INSERT INTO consultation_products(id) VALUES ('00000000-0000-0000-0000-000000000003');
                    INSERT INTO users(user_id) VALUES ('00000000-0000-0000-0000-000000000010');
                    """.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/changelog/scripts/34-create-membership-authoring.sql"));

            try (var result = connection.createStatement().executeQuery(
                    "SELECT pricing_model, billing_interval, currency FROM course_products")) {
                result.next();
                assertEquals("ONE_TIME", result.getString(1));
                assertEquals(null, result.getString(2));
                assertEquals("EUR", result.getString(3));
            }

            UUID membershipId = UUID.randomUUID();
            connection.createStatement().executeUpdate("""
                    INSERT INTO membership_products(
                        id,name,type,status,user_id,price,pricing_model,billing_interval,currency,customers,ordering_mode
                    ) VALUES (
                        '%s','Membership','MEMBERSHIP','DRAFT','00000000-0000-0000-0000-000000000010',
                        0,'RECURRING','MONTH','EUR',0,'NEWEST_FIRST'
                    )
                    """.formatted(membershipId));
            assertThrows(SQLException.class, () -> connection.createStatement().executeUpdate("""
                    INSERT INTO membership_products(
                        id,name,type,status,user_id,price,pricing_model,billing_interval,currency,customers,ordering_mode
                    ) VALUES (
                        '%s','Published','MEMBERSHIP','PUBLISHED','00000000-0000-0000-0000-000000000010',
                        1,'RECURRING','MONTH','EUR',0,'NEWEST_FIRST'
                    )
                    """.formatted(UUID.randomUUID())));

            UUID contentId = UUID.randomUUID();
            connection.createStatement().executeUpdate("""
                    INSERT INTO membership_content(id,membership_product_id,type,title,status,body)
                    VALUES ('%s','%s','POST','Welcome','PUBLISHED','Hello')
                    """.formatted(contentId, membershipId));
            connection.createStatement().executeUpdate("""
                    INSERT INTO membership_feed_entries(id,membership_product_id,kind,content_id,added_at)
                    VALUES ('%s','%s','CONTENT','%s',CURRENT_TIMESTAMP)
                    """.formatted(UUID.randomUUID(), membershipId, contentId));

            connection.createStatement().executeUpdate(
                    "DELETE FROM membership_products WHERE id = '" + membershipId + "'");
            assertEquals(0, count(connection, "membership_content"));
            assertEquals(0, count(connection, "membership_feed_entries"));
        }
    }

    private int count(Connection connection, String table) throws SQLException {
        try (var result = connection.createStatement().executeQuery("SELECT COUNT(*) FROM " + table)) {
            result.next();
            return result.getInt(1);
        }
    }
}
