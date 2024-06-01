package com.exporting.db.polling.status.configuration.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "event.retry-stalled")
public record EventRetryStalledProperty(int passedMinutesInProcessingToRetry) { }
