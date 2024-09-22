package com.exporting.db.listen.db.event;

public record Event<T>(long id, T payload) {
}
