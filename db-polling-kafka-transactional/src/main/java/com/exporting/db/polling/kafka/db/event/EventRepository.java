package com.exporting.db.polling.kafka.db.event;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class EventRepository {

    private final DataClassRowMapper<Event> eventDataClassRowMapper = DataClassRowMapper.newInstance(Event.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<Event> findAllNew(long previousId, int batchSize) {
        return jdbcTemplate.query("""
                select e.id, e.receiver, e.payload
                  from event e
                 where e.id > :previousId
                 order by e.id
                 limit :batchSize
                """,
                Map.of("previousId", previousId, "batchSize", batchSize),
                eventDataClassRowMapper
        );
    }

}
