package com.exporting.db.polling.kafka;

import com.exporting.db.polling.kafka.configuration.EventTestConfiguration;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.testcontainers.containers.KafkaContainer;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Import(EventTestConfiguration.class)
class EventOffsetServiceTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private KafkaContainer kafkaContainer;

    private KafkaConsumer<String, String> stringConsumer;

    @AfterEach
    public void tearDown() {
        stringConsumer.close();
    }

    @Test
    public void testSendingEvent() {
        stringConsumer = getStringConsumer(kafkaContainer, "output-topic");

        long eventId = 1;
        jdbcTemplate.update("""
                insert into event(id, receiver, payload)
                values(%d, 'output-topic', '{"id": "1", "data": [1, 2, 3, 4]}')
                """.formatted(eventId), Map.of()
        );

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> {
            ConsumerRecords<String, String> records = stringConsumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, String> record : records) {
                return record.key().equals(String.valueOf(eventId));
            }

            return false;
        });
    }

    private static KafkaConsumer<String, String> getStringConsumer(KafkaContainer kafkaContainer, String topicName) {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        KafkaConsumer<String, String> stringConsumer = new KafkaConsumer<>(props);

        stringConsumer.subscribe(Collections.singletonList("output-topic"));

        return stringConsumer;
    }


}