package com.exporting.db.polling.kafka;

import com.exporting.db.polling.kafka.consumer.EventOffsetConsumer;
import com.exporting.db.polling.kafka.db.event.Event;
import com.exporting.db.polling.kafka.db.event.EventService;
import com.exporting.db.polling.kafka.property.EventProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;

@Service
@RequiredArgsConstructor
public class EventOffsetService {

    private final EventService eventDbService;

    private final EventOffsetConsumer eventConsumer;
    private final EventProperty eventProperty;

    private final KafkaTemplate<String, String> transactionalStringArrayKafkaTemplate;

    @Scheduled(fixedDelay = 5, timeUnit = SECONDS)
    public void sendIfExist() {
        List<Event> events = findNextBatchForSend();

        if (events.isEmpty()) return;

        Map<String, List<Event>> groupedEventsByReceiver = groupEventsByReceiver(events);

        transactionalStringArrayKafkaTemplate.executeInTransaction(kafkaOperations -> {
            groupedEventsByReceiver.forEach(
                    (topicName, eventList) -> eventList.forEach(event -> sentEvent(kafkaOperations, topicName, event))
            );

            // Save the last offset
            kafkaOperations.send(eventProperty.offset().topicName(), String.valueOf(events.getLast().id()));

            return true;
        });
    }

    private Map<String, List<Event>> groupEventsByReceiver(List<Event> events) {
        return events.stream()
                .collect(Collectors.groupingBy(Event::receiver));
    }

    private void sentEvent(KafkaOperations<String, String> kafkaOperations, String topicName, Event event) {
        kafkaOperations.send(topicName, String.valueOf(event.id()), event.payload());
    }

    private List<Event> findNextBatchForSend() {
        long lastIdOffsetFromPreviousBatch = getLastCommitedOffset();

        return eventDbService.findAllNew(lastIdOffsetFromPreviousBatch);
    }

    private long getLastCommitedOffset() {
        return eventConsumer.consumerLastOffset().orElse(0L);
    }

}
