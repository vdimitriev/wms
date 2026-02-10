package mk.dmt.wms.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Entity class for persisting sensor measurements to the database.
 */
@Table("sensor_measurements")
public class SensorMeasurementEntity {

    @Id
    @Column("id")
    private Long id;

    @Column("sensor_id")
    private String sensorId;

    @Column("sensor_type")
    private String sensorType;

    @Column("measurement_value")
    private Double value;

    @Column("updated_at")
    private Instant timestamp;

    @Column("warehouse_id")
    private String warehouseId;

    @Column("created_at")
    private Instant createdAt;

    public SensorMeasurementEntity() {
    }

    public SensorMeasurementEntity(String sensorId, String sensorType, Double value,
                                   Instant timestamp, String warehouseId) {
        this.sensorId = sensorId;
        this.sensorType = sensorType;
        this.value = value;
        this.timestamp = timestamp;
        this.warehouseId = warehouseId;
        this.createdAt = Instant.now();
    }

    public static SensorMeasurementEntity fromMeasurement(SensorMeasurement measurement) {
        return new SensorMeasurementEntity(
            measurement.sensorId(),
            measurement.sensorType().name(),
            measurement.value(),
            measurement.timestamp(),
            measurement.warehouseId()
        );
    }

    public SensorMeasurement toMeasurement() {
        return new SensorMeasurement(
            sensorId,
            SensorType.valueOf(sensorType),
            value,
            timestamp,
            warehouseId
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
