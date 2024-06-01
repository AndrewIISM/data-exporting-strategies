package com.exporting.db.polling.status.db.event_type;

import com.exporting.db.polling.status.enumeration.EventType;

public record EventTypeToProcess(
        EventType eventType,
        int countToProcess
) {}
