package com.exporting.db.listen.db.event;

import com.exporting.db.listen.configuration.property.EventRetryStalledProperty;
import com.exporting.db.listen.enumeration.EventStatus;
import com.exporting.db.listen.enumeration.EventType;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    private final EventRetryStalledProperty retryStalledProperty;

    @Nullable
    public Long insert(EventType eventType, String jsonPayload) {
        return eventRepository.insert(eventType, jsonPayload);
    }

    public void updateStatusByIds(EventStatus status, Set<Long> ids) {
        eventRepository.updateStatus(status, ids);
    }

    public <T> List<Event<T>> findBatchTypeAndStatusNew(EventType type, int batchSize) {
        return eventRepository.findBatchTypeAndStatusNew(type, batchSize);
    }

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
