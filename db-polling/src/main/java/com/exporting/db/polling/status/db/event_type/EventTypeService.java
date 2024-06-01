package com.exporting.db.polling.status.db.event_type;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventTypeService {

    private final EventTypeRepository repository;

    public List<EventTypeToProcess> findExportTasksInfo() {
        return repository.findExportTasksType();
    }

}
