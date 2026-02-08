package mk.dmt.wms.controller;

import mk.dmt.wms.event.AlarmEvent;
import mk.dmt.wms.event.MeasurementEventBus;
import mk.dmt.wms.model.SensorMeasurement;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final MeasurementEventBus eventBus;

    public MonitoringController(MeasurementEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @GetMapping(value = "/measurements", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<SensorMeasurement> streamMeasurements() {
        return eventBus.subscribe();
    }

    @GetMapping(value = "/alarms", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AlarmEvent> streamAlarms() {
        // You'll need to add this to your CentralMonitoringService
        // For now, this shows the pattern
        return eventBus.subscribe()
                .filter(this::exceedsThreshold)
                .map(this::createAlarm);
    }

    private boolean exceedsThreshold(SensorMeasurement m) {
        return m.value() > m.sensorType().getDefaultThreshold();
    }

    private AlarmEvent createAlarm(SensorMeasurement m) {
        double threshold = m.sensorType().getDefaultThreshold();
        return AlarmEvent.of(m, threshold);
    }
}
