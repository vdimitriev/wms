package mk.dmt.wms.service;

import mk.dmt.wms.event.AlarmEvent;
import mk.dmt.wms.model.AlarmEventEntity;
import mk.dmt.wms.model.SensorMeasurement;
import mk.dmt.wms.model.SensorMeasurementEntity;
import mk.dmt.wms.repository.AlarmEventRepository;
import mk.dmt.wms.repository.SensorMeasurementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * Service for persisting measurements and alarms to the database.
 */
@Service
public class MeasurementPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(MeasurementPersistenceService.class);

    private final SensorMeasurementRepository measurementRepository;
    private final AlarmEventRepository alarmRepository;

    public MeasurementPersistenceService(SensorMeasurementRepository measurementRepository,
                                        AlarmEventRepository alarmRepository) {
        this.measurementRepository = measurementRepository;
        this.alarmRepository = alarmRepository;
    }

    /**
     * Save a sensor measurement to the database.
     */
    public Mono<SensorMeasurementEntity> saveMeasurement(SensorMeasurement measurement) {
        SensorMeasurementEntity entity = SensorMeasurementEntity.fromMeasurement(measurement);
        return measurementRepository.save(entity)
                .doOnSuccess(saved -> log.debug("Saved measurement to database: id={}", saved.getId()))
                .doOnError(error -> log.error("Failed to save measurement: {}", error.getMessage()));
    }

    /**
     * Save an alarm event to the database.
     */
    public Mono<AlarmEventEntity> saveAlarm(AlarmEvent alarm) {
        AlarmEventEntity entity = AlarmEventEntity.fromAlarm(alarm);
        return alarmRepository.save(entity)
                .doOnSuccess(saved -> log.debug("Saved alarm to database: id={}", saved.getId()))
                .doOnError(error -> log.error("Failed to save alarm: {}", error.getMessage()));
    }

    /**
     * Get measurement history for a specific sensor.
     */
    public Flux<SensorMeasurement> getMeasurementHistory(String sensorId, int limit) {
        return measurementRepository.findBySensorIdOrderByTimestampDesc(sensorId)
                .take(limit)
                .map(SensorMeasurementEntity::toMeasurement);
    }

    /**
     * Get recent measurements (last N minutes).
     */
    public Flux<SensorMeasurement> getRecentMeasurements(int minutes, int limit) {
        Instant after = Instant.now().minus(Duration.ofMinutes(minutes));
        return measurementRepository.findByTimestampAfterOrderByTimestampDesc(after)
                .take(limit)
                .map(SensorMeasurementEntity::toMeasurement);
    }

    /**
     * Get measurements by sensor type.
     */
    public Flux<SensorMeasurement> getMeasurementsBySensorType(String sensorType, int limit) {
        return measurementRepository.findLatestBySensorType(sensorType, limit)
                .map(SensorMeasurementEntity::toMeasurement);
    }

    /**
     * Get the latest N measurements.
     */
    public Flux<SensorMeasurement> getLatestMeasurements(int limit) {
        return measurementRepository.findLatestMeasurements(limit)
                .map(SensorMeasurementEntity::toMeasurement);
    }

    /**
     * Get alarm history.
     */
    public Flux<AlarmEventEntity> getAlarmHistory(int limit) {
        return alarmRepository.findLatestAlarms(limit);
    }

    /**
     * Get recent alarms (last N minutes).
     */
    public Flux<AlarmEventEntity> getRecentAlarms(int minutes) {
        Instant after = Instant.now().minus(Duration.ofMinutes(minutes));
        return alarmRepository.findByTimestampAfterOrderByTimestampDesc(after);
    }

    /**
     * Get total count of measurements.
     */
    public Mono<Long> getTotalMeasurementCount() {
        return measurementRepository.count();
    }

    /**
     * Get total count of alarms.
     */
    public Mono<Long> getTotalAlarmCount() {
        return alarmRepository.count();
    }
}
