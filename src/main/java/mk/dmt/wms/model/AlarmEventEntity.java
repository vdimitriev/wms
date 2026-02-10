package mk.dmt.wms.model;

import mk.dmt.wms.event.AlarmEvent;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Entity class for persisting alarm events to the database.
 */
@Table("alarm_events")
public class AlarmEventEntity {

    @Id
    @Column("id")
    private Long id;

    @Column("sensor_id")
    private String sensorId;

    @Column("sensor_type")
    private String sensorType;

    @Column("measurement_value")
    private Double value;

    @Column("threshold")
    private Double threshold;

    @Column("severity")
    private String severity;

    @Column("updated_at")
    private Instant timestamp;

    @Column("warehouse_id")
    private String warehouseId;

    @Column("created_at")
    private Instant createdAt;

    public AlarmEventEntity() {
    }

    public AlarmEventEntity(String sensorId, String sensorType, Double value,
                           Double threshold, String severity, Instant timestamp, String warehouseId) {
        this.sensorId = sensorId;
        this.sensorType = sensorType;
        this.value = value;
        this.threshold = threshold;
        this.severity = severity;
        this.timestamp = timestamp;
        this.warehouseId = warehouseId;
        this.createdAt = Instant.now();
    }

    public static AlarmEventEntity fromAlarm(AlarmEvent alarm) {
        return new AlarmEventEntity(
            alarm.measurement().sensorId(),
            alarm.measurement().sensorType().name(),
            alarm.measurement().value(),
            alarm.threshold(),
            alarm.severity().name(),
            alarm.measurement().timestamp(),
            alarm.measurement().warehouseId()
        );
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
