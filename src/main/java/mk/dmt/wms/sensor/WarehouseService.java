package mk.dmt.wms.sensor;

import io.netty.channel.socket.DatagramPacket;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import mk.dmt.wms.config.SensorConfig;
import mk.dmt.wms.event.MeasurementEventBus;
import mk.dmt.wms.model.SensorMeasurement;
import mk.dmt.wms.model.SensorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.netty.udp.UdpServer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Warehouse Service that collects data from various UDP sensors
 * and forwards measurements to the Central Monitoring Service via the event bus.
 */
@Service
public class WarehouseService {

    private static final Logger log = LoggerFactory.getLogger(WarehouseService.class);

    private final SensorConfig sensorConfig;
    private final SensorMessageParser messageParser;
    private final MeasurementEventBus eventBus;
    private final List<Disposable> disposables = new ArrayList<>();

    public WarehouseService(SensorConfig sensorConfig,
                           SensorMessageParser messageParser,
                           MeasurementEventBus eventBus) {
        this.sensorConfig = sensorConfig;
        this.messageParser = messageParser;
        this.eventBus = eventBus;
    }

    @PostConstruct
    public void startListeners() {
        log.info("Starting Warehouse Service UDP listeners...");

        // Start temperature sensor listener
        startUdpListener(sensorConfig.getTemperaturePort(), SensorType.TEMPERATURE);

        // Start humidity sensor listener
        startUdpListener(sensorConfig.getHumidityPort(), SensorType.HUMIDITY);

        log.info("Warehouse Service started. Listening for sensors:");
        log.info("  - Temperature sensors on UDP port {}", sensorConfig.getTemperaturePort());
        log.info("  - Humidity sensors on UDP port {}", sensorConfig.getHumidityPort());
    }

    private void startUdpListener(int port, SensorType sensorType) {
        Disposable disposable = UdpServer.create()
                .host(sensorConfig.getHost())
                .port(port)
                .handle((in, out) -> in.receiveObject()
                        .cast(DatagramPacket.class)
                        .doOnNext(packet -> processPacket(packet, sensorType))
                        .then())
                .bind()
                .doOnSuccess(connection -> log.info("UDP listener for {} bound to port {}",
                        sensorType.getDisplayName(), port))
                .doOnError(error -> log.error("Failed to bind UDP listener for {} on port {}: {}",
                        sensorType.getDisplayName(), port, error.getMessage()))
                .subscribe();

        disposables.add(disposable);
    }

    private void processPacket(DatagramPacket packet, SensorType sensorType) {
        String message = packet.content().toString(StandardCharsets.UTF_8);
        log.debug("Received {} sensor data: {}", sensorType.getDisplayName(), message);

        Optional<SensorMeasurement> measurement = messageParser.parse(message, sensorType);

        measurement.ifPresentOrElse(
                m -> {
                    log.info("Processed measurement: {}", m.toDisplayString());
                    eventBus.publish(m);
                },
                () -> log.warn("Failed to parse {} sensor message: {}", sensorType.getDisplayName(), message)
        );
    }

    @PreDestroy
    public void stopListeners() {
        log.info("Stopping Warehouse Service UDP listeners...");
        disposables.forEach(Disposable::dispose);
        eventBus.complete();
        log.info("Warehouse Service stopped.");
    }
}
