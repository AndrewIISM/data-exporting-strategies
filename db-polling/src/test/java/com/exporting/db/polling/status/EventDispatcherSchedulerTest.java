package com.exporting.db.polling.status;

import com.exporting.db.polling.status.configuration.EventTestConfiguration;
import com.exporting.db.polling.status.db.event.EventService;
import com.exporting.db.polling.status.enumeration.EventStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.exporting.db.polling.status.enumeration.EventType.MAGIC_EVENT;

@SpringBootTest
@Import(EventTestConfiguration.class)
class EventDispatcherSchedulerTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    public void testChangingStatusToSuccess() {
        long id = eventService.insert(MAGIC_EVENT, "{\"magicEntityId\": 1}");

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> {
            String eventStatus = jdbcTemplate.queryForObject("""
                        select status
                          from event
                         where id = :id
                        """,
                    Map.of("id", id),
                    String.class
            );

            return EventStatus.SUCCESS.name().equals(eventStatus);
        });
    }

    @Test
    public void testRetryingStalledEvent() {
        Long id = jdbcTemplate.queryForObject("""
                        insert into event (type_code, status, payload, created_at, updated_at)
                        values (:type, 'PROCESSING', :jsonPayload::jsonb, clock_timestamp(), clock_timestamp() - interval '15 minutes')
                        returning id
                        """,
                Map.of("type", MAGIC_EVENT.name(), "jsonPayload", "{\"magicEntityId\": 1}"),
                Long.class
        );

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> {
            int processingRetriesCount = jdbcTemplate.queryForObject("""
                        select processing_retries_count
                          from event
                         where id = :id
                        """,
                    Map.of("id", id),
                    Integer.class
            );

            return processingRetriesCount == 1;
        });
    }

    @Test
    public void testChangingStatusToError() {
        Long id = jdbcTemplate.queryForObject("""
                        insert into event (type_code, status, payload, created_at, updated_at, processing_retries_count)
                        values (:type, 'PROCESSING', :jsonPayload::jsonb, clock_timestamp(), clock_timestamp() - interval '15 minutes', 3)
                        returning id
                        """,
                Map.of("type", MAGIC_EVENT.name(), "jsonPayload", "{\"magicEntityId\": 1}"),
                Long.class
        );

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> {
            String status = jdbcTemplate.queryForObject("""
                        select status
                          from event
                         where id = :id
                        """,
                    Map.of("id", id),
                    String.class
            );

            return EventStatus.ERROR.name().equals(status);
        });
    }


}