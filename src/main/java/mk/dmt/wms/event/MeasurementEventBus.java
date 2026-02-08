package mk.dmt.wms.event;

import mk.dmt.wms.model.SensorMeasurement;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Reactive event bus for sensor measurements using Project Reactor's Sinks.
 * This provides a fully reactive way to publish and subscribe to measurement events.
 */
@Component
public class MeasurementEventBus {

    private final Sinks.Many<SensorMeasurement> measurementSink;
    private final Flux<SensorMeasurement> measurementFlux;

    public MeasurementEventBus() {
        // Create a multicast sink that replays the last 100 measurements for late subscribers
        this.measurementSink = Sinks.many().multicast().onBackpressureBuffer(1000);
        this.measurementFlux = measurementSink.asFlux();
    }

    /**
     * Publishes a measurement to all subscribers.
     *
     * @param measurement The sensor measurement to publish
     */
    public void publish(SensorMeasurement measurement) {
        measurementSink.tryEmitNext(measurement);
    }

    /**
     * Returns a Flux that emits all published measurements.
     * Multiple subscribers can subscribe to this Flux.
     *
     * @return Flux of sensor measurements
     */
    public Flux<SensorMeasurement> subscribe() {
        return measurementFlux;
    }

    /**
     * Completes the sink, signaling no more measurements will be published.
     */
    public void complete() {
        measurementSink.tryEmitComplete();
    }
}
