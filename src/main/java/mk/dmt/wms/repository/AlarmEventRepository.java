package mk.dmt.wms.repository;

import mk.dmt.wms.model.AlarmEventEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.Instant;

/**
 * Reactive repository for alarm events.
 */
@Repository
public interface AlarmEventRepository extends ReactiveCrudRepository<AlarmEventEntity, Long> {

    /**
     * Find alarms by sensor ID, ordered by timestamp descending.
     */
    Flux<AlarmEventEntity> findBySensorIdOrderByTimestampDesc(String sensorId);

    /**
     * Find alarms by severity, ordered by timestamp descending.
     */
    Flux<AlarmEventEntity> findBySeverityOrderByTimestampDesc(String severity);

    /**
     * Find recent alarms within a time range.
     */
    Flux<AlarmEventEntity> findByTimestampAfterOrderByTimestampDesc(Instant after);

    /**
     * Find the latest N alarms.
     */
    @Query("SELECT * FROM alarm_events ORDER BY timestamp DESC LIMIT :limit")
    Flux<AlarmEventEntity> findLatestAlarms(int limit);
}
