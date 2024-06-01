package com.exporting.db.polling.kafka.configuration;

import com.exporting.db.polling.kafka.property.EventProperty;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(EventProperty.class)
public class KafkaConfiguration {

    @Bean
    public KafkaTemplate<String, String> transactionalStringArrayKafkaTemplate(KafkaProperties kafkaGlobalProperties) {
        Map<String, Object> producerProperties = kafkaGlobalProperties.buildProducerProperties(null);

        DefaultKafkaProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(
                producerProperties,
                new StringSerializer(),
                new StringSerializer()
        );

        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public TopicPartition eventOffsetTopicPartition(EventProperty property) {
        return new TopicPartition(property.offset().topicName(), 0);
    }

    @Bean
    public Consumer<String, String> eventOffsetConsumer(EventProperty property,
                                                        ConsumerFactory<String, String> consumerFactory,
                                                        TopicPartition eventOffsetTopicPartition) {

        Consumer<String, String> eventOffsetConsumer = consumerFactory.createConsumer(property.offset().groupId(), "-event");

        eventOffsetConsumer.assign(List.of(eventOffsetTopicPartition));

        return eventOffsetConsumer;
    }

}
