package com.exporting.db.listen.configuration.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "event.retry-stalled")
public record EventRetryStalledProperty(int passedMinutesInProcessingToRetry) { }
