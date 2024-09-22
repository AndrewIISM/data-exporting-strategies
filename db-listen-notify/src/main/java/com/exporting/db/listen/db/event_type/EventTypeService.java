package com.exporting.db.listen.db.event_type;

import com.exporting.db.listen.enumeration.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class EventTypeService {

    private final EventTypeRepository repository;

    public Runnable createEventNotificationHandler(Consumer<List<EventType>> consumer) {
        return repository.createEventNotificationHandler(consumer);
    }

    public List<EventType> findEventsInfo() {
        return repository.findEventIdsByType();
    }

}
