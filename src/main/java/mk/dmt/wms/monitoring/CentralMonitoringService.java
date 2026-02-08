package mk.dmt.wms.monitoring;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import mk.dmt.wms.config.MonitoringConfig;
import mk.dmt.wms.event.AlarmEvent;
import mk.dmt.wms.event.MeasurementEventBus;
import mk.dmt.wms.model.SensorMeasurement;
import mk.dmt.wms.model.SensorType;
import mk.dmt.wms.service.MeasurementPersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;

/**
 * Central Monitoring Service that monitors measurements from all warehouses
 * and triggers alarms when thresholds are exceeded.
 */
@Service
public class CentralMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(CentralMonitoringService.class);

    // ANSI color codes for console output
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RESET = "\u001B[0m";

    private final MeasurementEventBus eventBus;
    private final MonitoringConfig config;
    private final MeasurementPersistenceService persistenceService;
    private Disposable subscription;

    public CentralMonitoringService(MeasurementEventBus eventBus,
                                   MonitoringConfig config,
                                   MeasurementPersistenceService persistenceService) {
        this.eventBus = eventBus;
        this.config = config;
        this.persistenceService = persistenceService;
    }

    @PostConstruct
    public void startMonitoring() {
        log.info("Starting Central Monitoring Service...");
        log.info("Configured thresholds:");
        log.info("  - Temperature: {}°C", config.getTemperatureThreshold());
        log.info("  - Humidity: {}%", config.getHumidityThreshold());

        subscription = eventBus.subscribe()
                .doOnNext(this::processMeasurement)
                .subscribe(
                        measurement -> {},
                        error -> log.error("Error in monitoring stream: {}", error.getMessage()),
                        () -> log.info("Monitoring stream completed")
                );

        log.info("Central Monitoring Service started. Monitoring all warehouses...");
    }

    private void processMeasurement(SensorMeasurement measurement) {
        // Persist measurement to database
        persistenceService.saveMeasurement(measurement)
                .subscribe(
                    entity -> log.debug("Persisted measurement: id={}", entity.getId()),
                    error -> log.error("Failed to persist measurement: {}", error.getMessage())
                );

        double threshold = getThreshold(measurement.sensorType());

        if (measurement.value() > threshold) {
            triggerAlarm(measurement, threshold);
        } else {
            log.debug("Measurement within normal range: {}", measurement.toDisplayString());
        }
    }

    private double getThreshold(SensorType sensorType) {
        return switch (sensorType) {
            case TEMPERATURE -> config.getTemperatureThreshold();
            case HUMIDITY -> config.getHumidityThreshold();
        };
    }

    private void triggerAlarm(SensorMeasurement measurement, double threshold) {
        AlarmEvent alarm = AlarmEvent.of(measurement, threshold);

        // Persist alarm to database
        persistenceService.saveAlarm(alarm)
                .subscribe(
                    entity -> log.debug("Persisted alarm: id={}", entity.getId()),
                    error -> log.error("Failed to persist alarm: {}", error.getMessage())
                );

        // Log with appropriate severity color
        String colorCode = switch (alarm.getSeverity()) {
            case "CRITICAL" -> ANSI_RED;
            case "HIGH" -> ANSI_RED;
            default -> ANSI_YELLOW;
        };

        // Print alarm to console with color formatting
        System.out.println(colorCode + alarm.toAlarmMessage() + ANSI_RESET);

        // Also log for persistent record
        log.warn("ALARM TRIGGERED: {} sensor {} exceeded threshold. Value: {}{}, Threshold: {}{}, Warehouse: {}",
                measurement.sensorType().getDisplayName(),
                measurement.sensorId(),
                measurement.value(),
                measurement.sensorType().getUnit(),
                threshold,
                measurement.sensorType().getUnit(),
                measurement.warehouseId());
    }

    @PreDestroy
    public void stopMonitoring() {
        log.info("Stopping Central Monitoring Service...");
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }
        log.info("Central Monitoring Service stopped.");
    }

    /**
     * Checks if a measurement exceeds its threshold.
     * Useful for testing.
     */
    public boolean exceedsThreshold(SensorMeasurement measurement) {
        double threshold = getThreshold(measurement.sensorType());
        return measurement.value() > threshold;
    }
}
