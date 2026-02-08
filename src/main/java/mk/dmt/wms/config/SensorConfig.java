package mk.dmt.wms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for sensor settings.
 */
@Configuration
@ConfigurationProperties(prefix = "sensor")
public class SensorConfig {

    private int temperaturePort = 3344;
    private int humidityPort = 3355;
    private String host = "0.0.0.0";

    public int getTemperaturePort() {
        return temperaturePort;
    }

    public void setTemperaturePort(int temperaturePort) {
        this.temperaturePort = temperaturePort;
    }

    public int getHumidityPort() {
        return humidityPort;
    }

    public void setHumidityPort(int humidityPort) {
        this.humidityPort = humidityPort;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
