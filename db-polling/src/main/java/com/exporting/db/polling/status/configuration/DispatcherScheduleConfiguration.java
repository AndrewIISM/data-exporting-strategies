package com.exporting.db.polling.status.configuration;

import com.exporting.db.polling.status.DispatcherScheduler;
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
    public DispatcherScheduler dispatcherScheduler(List<EventProcessingService> exportableTaskServices,
                                                   EventTypeService exportTaskInfoService,
                                                   EventService exportTaskService) {

        Map<EventType, EventProcessingService> exportTasks = exportableTaskServices.stream()
                .collect(Collectors.toMap(EventProcessingService::getType, Function.identity()));

        return new DispatcherScheduler(exportTaskInfoService, exportTaskService, exportTasks);
    }

}
