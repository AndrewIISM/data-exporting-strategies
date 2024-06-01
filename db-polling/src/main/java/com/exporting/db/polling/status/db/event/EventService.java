package com.exporting.db.polling.status.db.event;

import com.exporting.db.polling.status.configuration.property.EventRetryStalledProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    private final EventRetryStalledProperty retryStalledProperty;

    public void updateStalledToAnotherRetry() {
        List<Long> retriedTaskIds =
                eventRepository.updateStalledToAnotherRetry(retryStalledProperty.passedMinutesInProcessingToRetry());

        if (!retriedTaskIds.isEmpty()) {
            log.warn("Retried stalled export tasks: {}", retriedTaskIds);
        }
    }

    public void updateStalledToErrorStatus() {
        List<Long> failedTaskIds =
                eventRepository.updateStalledToErrorStatus(retryStalledProperty.passedMinutesInProcessingToRetry());

        if (!failedTaskIds.isEmpty()) {
            log.error("Export tasks moved to ERROR status: {}", failedTaskIds);
        }
    }

}
