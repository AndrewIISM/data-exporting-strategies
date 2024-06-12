package com.exporting.db.polling.kafka.configuration;

import com.exporting.db.polling.kafka.property.EventProperty;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;

import java.util.List;

@Configuration
@EnableConfigurationProperties(EventProperty.class)
public class KafkaConfiguration {

    @Bean
    public TopicPartition eventOffsetTopicPartition(EventProperty property) {
        return new TopicPartition(property.offset().topicName(), 0);
    }

    @Bean
    public Consumer<String, String> eventOffsetKafkaConsumer(EventProperty property,
                                                             ConsumerFactory<String, String> consumerFactory,
                                                             TopicPartition eventOffsetTopicPartition) {

        Consumer<String, String> eventOffsetConsumer = consumerFactory.createConsumer(property.offset().groupId(), "-event");

        eventOffsetConsumer.assign(List.of(eventOffsetTopicPartition));

        return eventOffsetConsumer;
    }

}
