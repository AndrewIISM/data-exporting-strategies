package com.exporting.db.polling.status.event;

import com.exporting.db.polling.status.EventProcessingService;
import com.exporting.db.polling.status.db.event.Event;
import com.exporting.db.polling.status.db.event.EventService;
import com.exporting.db.polling.status.enumeration.EventStatus;
import com.exporting.db.polling.status.enumeration.EventType;
import com.exporting.db.polling.status.payload_type.MagicEventPayload;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MagicEventProcessingServiceTestImpl implements EventProcessingService {

    private static final int DEFAULT_MAGIC_BATCH_SIZE = 1000;

    private final EventService eventService;

    public MagicEventProcessingServiceTestImpl(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public void process() {
        List<Event<MagicEventPayload>> events = eventService.findBatchTypeAndStatusNew(getType(), DEFAULT_MAGIC_BATCH_SIZE);

        if (!events.isEmpty()) {
            // do some business logic...

            Set<Long> eventIds = events.stream()
                    .map(Event::id)
                    .collect(Collectors.toSet());

            eventService.updateStatusByIds(EventStatus.SUCCESS, eventIds);
        }
    }

    @Override
    public EventType getType() {
        return EventType.MAGIC_EVENT;
    }

}
