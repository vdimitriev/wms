package mk.dmt.wms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for monitoring thresholds.
 */
@Configuration
@ConfigurationProperties(prefix = "monitoring")
public class MonitoringConfig {

    private double temperatureThreshold = 35.0;
    private double humidityThreshold = 50.0;

    public double getTemperatureThreshold() {
        return temperatureThreshold;
    }

    public void setTemperatureThreshold(double temperatureThreshold) {
        this.temperatureThreshold = temperatureThreshold;
    }

    public double getHumidityThreshold() {
        return humidityThreshold;
    }

    public void setHumidityThreshold(double humidityThreshold) {
        this.humidityThreshold = humidityThreshold;
    }
}
