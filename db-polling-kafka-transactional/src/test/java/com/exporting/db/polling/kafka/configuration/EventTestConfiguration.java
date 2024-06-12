package com.exporting.db.polling.kafka.configuration;

import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@TestConfiguration(proxyBeanMethods = false)
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
    @ServiceConnection
    public KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.1"))
                .withEnv("KAFKA_BROKER_ID", "1")
                .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true");
    }

}
