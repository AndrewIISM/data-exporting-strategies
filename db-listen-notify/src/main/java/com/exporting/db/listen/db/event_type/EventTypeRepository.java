package com.exporting.db.listen.db.event_type;

import com.exporting.db.listen.enumeration.EventType;
import lombok.RequiredArgsConstructor;
import org.postgresql.PGNotification;
import org.postgresql.jdbc.PgConnection;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Repository
@RequiredArgsConstructor
public class EventTypeRepository {

    private final static String NOTIFICATION_CHANNEL_NAME = "events";

    private final EventTypeToProcessRowMapper eventTypeToProcessRowMapper = new EventTypeToProcessRowMapper();

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Runnable createEventNotificationHandler(Consumer<List<EventType>> consumer) {
        return () -> {
            jdbcTemplate.getJdbcTemplate().execute((ConnectionCallback<Object>) con -> {
                con.createStatement().execute("LISTEN " + NOTIFICATION_CHANNEL_NAME);
                PgConnection pgConnection = con.unwrap(PgConnection.class);

                while (Thread.currentThread().isAlive()) {
                    PGNotification[] nts = pgConnection.getNotifications(10000);
                    if (nts == null) {
                        continue;
                    }

                    List<EventType> eventTypes = Arrays.stream(nts)
                            .map(PGNotification::getName)
                            .map(EventType::valueOf)
                            .toList();

                    consumer.accept(eventTypes);
                }

                return null;
            });
        };
    }

    public List<EventType> findEventIdsByType() {
        return jdbcTemplate.query("""
                select e.type_code
                  from event e
                 where e.status = 'NEW'
                 group by e.type_code
                 having count(e.id) > 0
                """, Map.of(), eventTypeToProcessRowMapper);
    }

    static class EventTypeToProcessRowMapper implements RowMapper<EventType> {

        @Override
        public EventType mapRow(ResultSet rs, int rowNum) throws SQLException {
            return EventType.valueOf(rs.getString("type_code"));
        }

    }

}
