package com.exporting.db.polling.status.db.event_type;

import com.exporting.db.polling.status.enumeration.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class EventTypeRepository {

    private final EventTypeToProcessRowMapper eventTypeToProcessRowMapper = new EventTypeToProcessRowMapper();

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<EventTypeToProcess> findExportTasksType() {
        return jdbcTemplate.query("""
                select e.type_code,
                       count(e.id) count_to_process
                  from event e
                 where e.status = 'NEW'
                 group by e.type_code
                """, Map.of(), eventTypeToProcessRowMapper);
    }

    static class EventTypeToProcessRowMapper implements RowMapper<EventTypeToProcess> {

        @Override
        public EventTypeToProcess mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new EventTypeToProcess(
                    EventType.valueOf(rs.getString("type_code")),
                    rs.getInt("count_to_process")
            );
        }

    }

}
