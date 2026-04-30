package org.fmazmz.springbootai.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthProviderColumnMigration implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    public AuthProviderColumnMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        String dataType = jdbcTemplate.queryForObject(
                """
                SELECT data_type
                FROM information_schema.columns
                WHERE table_schema = current_schema()
                  AND table_name = 'users'
                  AND column_name = 'auth_provider'
                """,
                String.class
        );

        if (dataType == null) {
            return;
        }

        if ("smallint".equalsIgnoreCase(dataType) || "integer".equalsIgnoreCase(dataType) || "bigint".equalsIgnoreCase(dataType)) {
            jdbcTemplate.execute(
                    """
                    ALTER TABLE users
                    ALTER COLUMN auth_provider TYPE VARCHAR(32)
                    USING (
                        CASE
                            WHEN auth_provider = 0 THEN 'GITHUB'
                            ELSE auth_provider::text
                        END
                    )
                    """
            );
        }
    }
}
