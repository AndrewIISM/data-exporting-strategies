package com.exporting.db.listen;

import com.exporting.db.listen.enumeration.EventType;

public interface EventProcessingService {

    void process();

    EventType getType();

}
