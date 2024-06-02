package com.exporting.db.polling.status.configuration;

import com.exporting.db.polling.status.EventProcessingService;
import com.exporting.db.polling.status.db.event.EventService;
import com.exporting.db.polling.status.event.MagicEventProcessingServiceTestImpl;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@TestConfiguration
public class EventTestConfiguration {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("huntress/postgres-partman:14").asCompatibleSubstituteFor("postgres"))
                .withNetworkAliases("postgres")
                .withDatabaseName("event")
                .withUsername("event")
                .withCopyFileToContainer(
                        MountableFile.forClasspathResource("initdb"),
                        "/docker-entrypoint-initdb.d/"
                )
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(PostgreSQLContainer.class)));
    }

    @Bean
    public EventProcessingService magicEventProcessingService(EventService eventService) {
        return new MagicEventProcessingServiceTestImpl(eventService);
    }

}
