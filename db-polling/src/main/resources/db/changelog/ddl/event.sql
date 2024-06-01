--liquibase formatted sql

--changeset failOnError:true

create table event_type (
    code            varchar not null,
    source          varchar not null,
    description     varchar not null,
    max_retry_count int     not null default 3,

    primary key (code)
);

----

create sequence s_event;

create table event (
    id                       bigint                              default nextval('s_event'),
    type_code         varchar                   not null,
    status                   varchar                   not null,
    created_at               timestamp with time zone  not null,
    updated_at               timestamp with time zone  not null  default clock_timestamp(),
    processing_retries_count int                       not null default 0,

    primary key (id)
) partition by list(status);

alter table event add constraint event_fk1 foreign key (type_code) references event_type (code);
alter table event add constraint event_ch1 check (status in ('NEW', 'PROCESSING', 'SUCCESS', 'ERROR'));
