package com.exporting.db.polling.kafka.db.event;

import com.exporting.db.polling.kafka.property.EventProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    private final EventProperty eventProperty;

    public List<Event> findAllNew(long previousId) {
        return eventRepository.findAllNew(previousId, eventProperty.batchSize());
    }

}
