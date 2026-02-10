package mk.dmt.wms.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Database configuration for Flyway migrations.
 * Spring Data R2DBC uses reactive connections, but Flyway needs a traditional JDBC DataSource.
 */
@Configuration
public class DatabaseConfig {

    @Value("${spring.flyway.url}")
    private String url;

    @Value("${spring.flyway.user}")
    private String username;

    @Value("${spring.flyway.password}")
    private String password;

    @Bean(initMethod = "migrate")
    public Flyway flyway() {
        return new Flyway(Flyway.configure()
                .baselineOnMigrate(true)
                .placeholderReplacement(false)
                .dataSource(url, username, password)
        );
    }
}

