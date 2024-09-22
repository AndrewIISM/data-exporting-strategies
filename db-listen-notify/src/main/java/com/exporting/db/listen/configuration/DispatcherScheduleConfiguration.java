package com.exporting.db.listen.configuration;

import com.exporting.db.listen.EventDispatcher;
import com.exporting.db.listen.EventProcessingService;
import com.exporting.db.listen.configuration.property.EventRetryStalledProperty;
import com.exporting.db.listen.db.event.EventService;
import com.exporting.db.listen.db.event_type.EventTypeService;
import com.exporting.db.listen.enumeration.EventType;
import org.springframework.boot.CommandLineRunner;
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
    public EventDispatcher dispatcherScheduler(List<EventProcessingService> eventProcessingServices,
                                               EventTypeService eventTypeService,
                                               EventService eventService) {

        Map<EventType, EventProcessingService> events = eventProcessingServices.stream()
                .collect(Collectors.toMap(EventProcessingService::getType, Function.identity()));

        return new EventDispatcher(eventTypeService, eventService, events);
    }

    @Bean
    public CommandLineRunner startEventListener(EventDispatcher dispatcherScheduler, EventTypeService eventTypeService) {
        return (args) -> {
            Runnable eventNotificationHandler = eventTypeService.createEventNotificationHandler(dispatcherScheduler::export);
            Thread t = new Thread(eventNotificationHandler, "event-listener");
            t.start();
        };
    }

}
