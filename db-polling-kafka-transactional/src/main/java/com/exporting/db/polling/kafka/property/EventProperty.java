package com.exporting.db.polling.kafka.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;


@ConfigurationProperties(prefix = "event")
public record EventProperty(
        int batchSize,
        @NestedConfigurationProperty
        EventOffsetProperty offset
) {
    public record EventOffsetProperty(String topicName, String groupId) { }
}
