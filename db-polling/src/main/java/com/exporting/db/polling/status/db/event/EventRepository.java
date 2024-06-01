package com.exporting.db.polling.status.db.event;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class EventRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

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
