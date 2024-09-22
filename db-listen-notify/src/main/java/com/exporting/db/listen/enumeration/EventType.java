package com.exporting.db.listen.enumeration;

import com.exporting.db.listen.payload_type.MagicEventPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {

    MAGIC_EVENT(MagicEventPayload.class);

    private final Class<?> eventPayload;

}
