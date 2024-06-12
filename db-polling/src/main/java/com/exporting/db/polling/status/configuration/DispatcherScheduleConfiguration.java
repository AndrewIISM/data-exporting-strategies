package com.exporting.db.polling.status.configuration;

import com.exporting.db.polling.status.EventDispatcherScheduler;
import com.exporting.db.polling.status.EventProcessingService;
import com.exporting.db.polling.status.configuration.property.EventRetryStalledProperty;
import com.exporting.db.polling.status.db.event.EventService;
import com.exporting.db.polling.status.db.event_type.EventTypeService;
import com.exporting.db.polling.status.enumeration.EventType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({ EventRetryStalledProperty.class })
public class DispatcherScheduleConfiguration {

    @Bean
    public EventDispatcherScheduler dispatcherScheduler(List<EventProcessingService> eventProcessingServices,
                                                        EventTypeService eventTypeService,
                                                        EventService eventService) {

        Map<EventType, EventProcessingService> exportTasks = eventProcessingServices.stream()
                .collect(Collectors.toMap(EventProcessingService::getType, Function.identity()));

        return new EventDispatcherScheduler(eventTypeService, eventService, exportTasks);
    }

}
