package com.exporting.db.listen;

import com.exporting.db.listen.db.event.EventService;
import com.exporting.db.listen.db.event_type.EventTypeService;
import com.exporting.db.listen.enumeration.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@RequiredArgsConstructor
public class EventDispatcher {

    private final EventTypeService eventTypeService;
    private final EventService eventService;

    private final Map<EventType, EventProcessingService> eventServices;

    @Scheduled(fixedRateString = "${event.dispatcher.stalled-seconds-rate}", timeUnit = SECONDS)
    public void processStalled() {
        eventService.updateStalledToAnotherRetry();
        eventService.updateStalledToErrorStatus();
    }

    @Scheduled(fixedRateString = "${event.dispatcher.export-seconds-rate}", timeUnit = SECONDS)
    public void exportBySchedule() {
        export(eventTypeService.findEventsInfo());
    }

    public void export(List<EventType> types) {
        types.forEach(this::processTask);
    }

    private void processTask(EventType eventType) {
        var service = eventServices.get(eventType);
        if (service == null) {
            throw new RuntimeException("Service wasn't found by eventTypeCode: " + eventType);
        }

        service.process();
    }

}
