--liquibase formatted sql

--changeset failOnError:true

create sequence s_event;

create table event (
    id         bigint                            default nextval('s_event'),
    created_at timestamp with time zone not null default clock_timestamp(),
    receiver   varchar                  not null,
    payload    jsonb                    not null,

    primary key (id)
) partition by range (id);

select ${DB_PARTMAN_SCHEMA}.create_parent(
    p_parent_table := '${DB_SCHEMA}.event',
    p_control := 'id',
    p_type := 'native',
    p_interval := '1000000',
    p_premake := 1
);

----

update ${DB_PARTMAN_SCHEMA}.part_config
   set infinite_time_partitions=true
 where parent_table in ('event');
