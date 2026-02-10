package mk.dmt.wms;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

/**
 * Simple test to verify database schema is created correctly.
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class DatabaseSchemaTest {

    @Autowired
    private DatabaseClient databaseClient;

    @Test
    void shouldHaveSensorMeasurementsTable() {
        // Query to check if the table exists
        String sql = "select count(*) as cnt from sensor_measurements";

        StepVerifier.create(
            databaseClient.sql(sql)
                .fetch()
                .one()
        )
        .expectNextMatches(result -> result.containsKey("cnt"))
        .verifyComplete();
    }

    @Test
    void shouldHaveAlarmEventsTable() {
        // Query to check if the table exists
        String sql = "select count(*) as cnt from alarm_events";

        StepVerifier.create(
            databaseClient.sql(sql)
                .fetch()
                .one()
        )
        .expectNextMatches(result -> result.containsKey("cnt"))
        .verifyComplete();
    }
}

