package com.exporting.db.polling.status.db.event;

public record Event<T>(long id, T payload) {
}
