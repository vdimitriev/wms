package mk.dmt.wms.controller;

import mk.dmt.wms.model.AlarmEventEntity;
import mk.dmt.wms.model.SensorMeasurement;
import mk.dmt.wms.service.MeasurementPersistenceService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST controller for querying historical measurement and alarm data.
 */
@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final MeasurementPersistenceService persistenceService;

    public HistoryController(MeasurementPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    /**
     * Get the latest measurements (default: last 100).
     */
    @GetMapping("/measurements")
    public Flux<SensorMeasurement> getLatestMeasurements(
            @RequestParam(defaultValue = "100") int limit) {
        return persistenceService.getLatestMeasurements(limit);
    }

    /**
     * Get measurements by sensor type.
     */
    @GetMapping("/measurements/type/{sensorType}")
    public Flux<SensorMeasurement> getMeasurementsByType(
            @PathVariable String sensorType,
            @RequestParam(defaultValue = "100") int limit) {
        return persistenceService.getMeasurementsBySensorType(sensorType, limit);
    }

    /**
     * Get measurement history for a specific sensor.
     */
    @GetMapping("/measurements/sensor/{sensorId}")
    public Flux<SensorMeasurement> getMeasurementHistory(
            @PathVariable String sensorId,
            @RequestParam(defaultValue = "100") int limit) {
        return persistenceService.getMeasurementHistory(sensorId, limit);
    }

    /**
     * Get recent measurements (last N minutes).
     */
    @GetMapping("/measurements/recent")
    public Flux<SensorMeasurement> getRecentMeasurements(
            @RequestParam(defaultValue = "60") int minutes,
            @RequestParam(defaultValue = "100") int limit) {
        return persistenceService.getRecentMeasurements(minutes, limit);
    }

    /**
     * Get alarm history (default: last 100).
     */
    @GetMapping("/alarms")
    public Flux<AlarmEventEntity> getAlarmHistory(
            @RequestParam(defaultValue = "100") int limit) {
        return persistenceService.getAlarmHistory(limit);
    }

    /**
     * Get recent alarms (last N minutes).
     */
    @GetMapping("/alarms/recent")
    public Flux<AlarmEventEntity> getRecentAlarms(
            @RequestParam(defaultValue = "60") int minutes) {
        return persistenceService.getRecentAlarms(minutes);
    }

    /**
     * Get statistics about stored data.
     */
    @GetMapping("/stats")
    public Mono<Statistics> getStatistics() {
        Mono<Long> measurementCount = persistenceService.getTotalMeasurementCount();
        Mono<Long> alarmCount = persistenceService.getTotalAlarmCount();

        return Mono.zip(measurementCount, alarmCount)
                .map(tuple -> new Statistics(tuple.getT1(), tuple.getT2()));
    }

    /**
     * Statistics record for database contents.
     */
    public record Statistics(Long totalMeasurements, Long totalAlarms) {}
}
