package com.exporting.db.polling.status.enumeration;

import com.exporting.db.polling.status.payload_type.MagicEventPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
    MAGIC_EVENT(MagicEventPayload.class);

    private final Class<?> eventPayload;

}
