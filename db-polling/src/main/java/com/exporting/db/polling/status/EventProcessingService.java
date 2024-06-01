package com.exporting.db.polling.status;

import com.exporting.db.polling.status.enumeration.EventType;

public interface EventProcessingService {

    void process();

    EventType getType();

}
