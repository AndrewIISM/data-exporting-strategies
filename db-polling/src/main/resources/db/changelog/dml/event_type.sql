--liquibase formatted sql

--changeset failOnError:true

insert into event_type(code, source, description)
values ('MAGIC_EVENT', 'Comment for the event type');

