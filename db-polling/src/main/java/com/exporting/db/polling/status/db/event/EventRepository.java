package com.exporting.db.polling.status.db.event;

import com.exporting.db.polling.status.enumeration.EventStatus;
import com.exporting.db.polling.status.enumeration.EventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class EventRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final ObjectMapper jsonMapper;

    @Nullable
    public Long insert(EventType type, String jsonPayload) {
        return jdbcTemplate.queryForObject("""
                insert into event (type_code, status, payload, created_at)
                values (:type, 'NEW', :jsonPayload::jsonb, clock_timestamp())
                returning id
                """,
                Map.of("type", type.name(), "jsonPayload", jsonPayload),
                Long.class);
    }

    public void updateStatus(EventStatus newStatus, Set<Long> ids) {
        jdbcTemplate.update("""
                update event
                   set status = :newStatus,
                       updated_at = clock_timestamp()
                 where id in (:ids)
                """,
                Map.of("newStatus", newStatus.name(), "ids", ids)
        );
    }

    public <T> List<Event<T>> findBatchTypeAndStatusNew(EventType eventType, int batchSize) {
        Class<T> eventTypePayload = (Class<T>) eventType.getEventPayload();

        EventRowMapper<T> eventRowMapper = new EventRowMapper<>(jsonMapper, eventTypePayload);

        return jdbcTemplate.query("""
                update event
                   set status = 'PROCESSING',
                       updated_at = clock_timestamp()
                 where id = any(select et.id
                                  from event et
                                 where et.status = 'NEW'
                                   and et.type_code = :code
                                 limit :batchSize for update skip locked)
                 returning id, payload
                """,
                Map.of("code", eventType.name(), "batchSize", batchSize),
                eventRowMapper
        );
    }

    @RequiredArgsConstructor
    public static class EventRowMapper<T> implements RowMapper<Event<T>> {

        private final ObjectMapper jsonMapper;
        private final Class<T> eventTypePayload;

        @Override
        @SneakyThrows
        public Event<T> mapRow(ResultSet rs, int rowNum) {
            long id = rs.getLong("id");
            T payload = jsonMapper.readValue(rs.getString("payload"), eventTypePayload);

            return new Event<>(id, payload);
        }

    }

    public List<Long> updateStalledToAnotherRetry(int passedMinutesInProcessingToRetry) {
        return jdbcTemplate.queryForList("""
                with stalled as (
                    select e.id
                      from event e
                      join event_type et on et.code = e.type_code
                     where extract(epoch from clock_timestamp() - e.updated_at) / 60 >= :passedMinutesInProcessingToRetry
                       and e.status = 'PROCESSING'
                       and e.processing_retries_count < et.max_retry_count
                )
                update event ue
                   set status = 'NEW',
                       updated_at = clock_timestamp(),
                       processing_retries_count = processing_retries_count + 1
                  from stalled se
                 where ue.id = se.id
                returning ue.id
                """,
                Map.of("passedMinutesInProcessingToRetry", passedMinutesInProcessingToRetry),
                Long.class
        );
    }

    public List<Long> updateStalledToErrorStatus(int passedMinutesInProcessingToRetry) {
        return jdbcTemplate.queryForList("""
                with stalled as (
                    select e.id
                      from event e
                      join event_type et on et.code = e.type_code
                     where extract(epoch from clock_timestamp() - e.updated_at) / 60 >= :passedMinutesInProcessingToRetry
                       and e.status = 'PROCESSING'
                       and e.processing_retries_count >= et.max_retry_count
                )
                update event ue
                   set status = 'ERROR',
                       updated_at = clock_timestamp()
                  from stalled se
                 where ue.id = se.id
                returning ue.id
                """,
                Map.of("passedMinutesInProcessingToRetry", passedMinutesInProcessingToRetry),
                Long.class
        );
    }

}
