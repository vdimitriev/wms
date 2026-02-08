package mk.dmt.wms.model;

/**
 * Enum representing the types of sensors in the warehouse.
 */
public enum SensorType {
    TEMPERATURE("temperature", 35.0, "°C"),
    HUMIDITY("humidity", 50.0, "%");

    private final String displayName;
    private final double defaultThreshold;
    private final String unit;

    SensorType(String displayName, double defaultThreshold, String unit) {
        this.displayName = displayName;
        this.defaultThreshold = defaultThreshold;
        this.unit = unit;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getDefaultThreshold() {
        return defaultThreshold;
    }

    public String getUnit() {
        return unit;
    }
}
