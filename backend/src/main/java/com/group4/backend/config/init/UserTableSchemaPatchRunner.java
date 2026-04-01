package com.group4.backend.config.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Hibernate {@code ddl-auto=update} does not always ALTER existing H2 file databases when
 * new columns are added to entities. This runner adds {@code User} moderation columns so older
 * {@code collabry} DB files still boot.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserTableSchemaPatchRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public UserTableSchemaPatchRunner(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection c = dataSource.getConnection()) {
            String url = c.getMetaData().getURL();
            if (!url.contains(":h2:")) {
                return;
            }
        }
        jdbcTemplate.execute(
                "ALTER TABLE users ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE NOT NULL");
        jdbcTemplate.execute(
                "ALTER TABLE users ADD COLUMN IF NOT EXISTS flagged BOOLEAN DEFAULT FALSE NOT NULL");
        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS created_at TIMESTAMP");
    }
}
