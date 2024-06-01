package com.exporting.db.polling.kafka.consumer;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EventOffsetConsumer {

    private final Consumer<String, String> eventOffsetConsumer;
    private final TopicPartition eventOffsetTopicPartition;

    public Optional<Long> consumerLastOffset() {
        eventOffsetConsumer.seekToEnd(List.of(eventOffsetTopicPartition));

        return getLastOffset(eventOffsetConsumer.position(eventOffsetTopicPartition));
    }

    private Optional<Long> getLastOffset(long currentPosition) {
        long lastRecordIndex = currentPosition - 1;
        if (lastRecordIndex < 0) {
            return Optional.empty();
        }

        eventOffsetConsumer.seek(eventOffsetTopicPartition, lastRecordIndex);
        ConsumerRecords<String, String> records = eventOffsetConsumer.poll(Duration.ofMinutes(1));

        if (records.count() > 1) {
            throw new RuntimeException("More than one records were fount in the offset topic: " + records.count());
        }

        for (ConsumerRecord<String, String> record : records) {
            return Optional.of(Long.parseLong(record.value()));
        }

        return getLastOffset(lastRecordIndex);
    }

}
