package com.exporting.db.polling.status;

import com.exporting.db.polling.status.db.event.EventService;
import com.exporting.db.polling.status.db.event_type.EventTypeService;
import com.exporting.db.polling.status.db.event_type.EventTypeToProcess;
import com.exporting.db.polling.status.enumeration.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@RequiredArgsConstructor
public class DispatcherScheduler {

    private final EventTypeService exportTaskTypeService;
    private final EventService exportTaskService;

    private final Map<EventType, EventProcessingService> exportTaskServices;

    @Scheduled(fixedRateString = "${event.dispatcher.stalled-seconds-rate}", timeUnit = SECONDS)
    public void processStalled() {
        exportTaskService.updateStalledToAnotherRetry();
        exportTaskService.updateStalledToErrorStatus();
    }

    @Scheduled(fixedRateString = "${event.dispatcher.export-seconds-rate}", timeUnit = SECONDS)
    public void export() {
        List<EventTypeToProcess> exportTasksToProcess = exportTaskTypeService.findExportTasksInfo();

        exportTasksToProcess.stream()
                .filter(task -> task.countToProcess() > 0)
                .forEach(this::processTask);
    }

    private void processTask(EventTypeToProcess eventType) {
        var service = exportTaskServices.get(eventType.eventType());
        if (service == null) {
            throw new RuntimeException("Service wasn't found by eventTypeCode: " + eventType.eventType());
        }

        service.process();
    }

}
