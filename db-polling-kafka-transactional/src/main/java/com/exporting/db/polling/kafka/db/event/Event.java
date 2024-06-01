package com.exporting.db.polling.kafka.db.event;

public record Event(
        long id,
        String receiver,
        String payload
) { }
