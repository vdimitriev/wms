package mk.dmt.wms.repository;

import mk.dmt.wms.model.SensorMeasurementEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.Instant;

/**
 * Reactive repository for sensor measurements.
 */
@Repository
public interface SensorMeasurementRepository extends ReactiveCrudRepository<SensorMeasurementEntity, Long> {

    /**
     * Find measurements by sensor ID, ordered by timestamp descending.
     */
    Flux<SensorMeasurementEntity> findBySensorIdOrderByTimestampDesc(String sensorId);

    /**
     * Find measurements by sensor type, ordered by timestamp descending.
     */
    Flux<SensorMeasurementEntity> findBySensorTypeOrderByTimestampDesc(String sensorType);

    /**
     * Find recent measurements within a time range.
     */
    Flux<SensorMeasurementEntity> findByTimestampAfterOrderByTimestampDesc(Instant after);

    /**
     * Find recent measurements by sensor ID within a time range.
     */
    Flux<SensorMeasurementEntity> findBySensorIdAndTimestampAfterOrderByTimestampDesc(String sensorId, Instant after);

    /**
     * Find the latest N measurements.
     */
    @Query("SELECT * FROM sensor_measurements ORDER BY updated_at DESC LIMIT :limit")
    Flux<SensorMeasurementEntity> findLatestMeasurements(int limit);

    /**
     * Find the latest N measurements for a specific sensor type.
     */
    @Query("SELECT * FROM sensor_measurements WHERE sensor_type = :sensorType ORDER BY updated_at DESC LIMIT :limit")
    Flux<SensorMeasurementEntity> findLatestBySensorType(String sensorType, int limit);
}
