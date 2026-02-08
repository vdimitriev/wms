package mk.dmt.wms.sensor;

import mk.dmt.wms.model.SensorMeasurement;
import mk.dmt.wms.model.SensorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for sensor messages received via UDP.
 * Expected format: "sensor_id=t1; value=30" or "sensor_id=h1; value=40"
 */
@Component
public class SensorMessageParser {

    private static final Logger log = LoggerFactory.getLogger(SensorMessageParser.class);

    // Pattern to match: sensor_id=xxx; value=yyy (with optional spaces)
    private static final Pattern MESSAGE_PATTERN =
            Pattern.compile("sensor_id\\s*=\\s*([^;]+);\\s*value\\s*=\\s*([\\d.]+)", Pattern.CASE_INSENSITIVE);

    /**
     * Parses a raw UDP message into a SensorMeasurement.
     *
     * @param message    The raw message string
     * @param sensorType The type of sensor (determined by the UDP port)
     * @return Optional containing the parsed measurement, or empty if parsing fails
     */
    public Optional<SensorMeasurement> parse(String message, SensorType sensorType) {
        if (message == null || message.isBlank()) {
            log.warn("Received empty or null message");
            return Optional.empty();
        }

        String trimmedMessage = message.trim();
        Matcher matcher = MESSAGE_PATTERN.matcher(trimmedMessage);

        if (!matcher.find()) {
            log.warn("Failed to parse message: '{}'. Expected format: sensor_id=xxx; value=yyy", trimmedMessage);
            return Optional.empty();
        }

        try {
            String sensorId = matcher.group(1).trim();
            double value = Double.parseDouble(matcher.group(2).trim());

            SensorMeasurement measurement = SensorMeasurement.of(sensorId, sensorType, value);
            log.debug("Parsed measurement: {}", measurement.toDisplayString());

            return Optional.of(measurement);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse value from message: '{}'. Error: {}", trimmedMessage, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Parses a temperature sensor message.
     */
    public Optional<SensorMeasurement> parseTemperature(String message) {
        return parse(message, SensorType.TEMPERATURE);
    }

    /**
     * Parses a humidity sensor message.
     */
    public Optional<SensorMeasurement> parseHumidity(String message) {
        return parse(message, SensorType.HUMIDITY);
    }
}
