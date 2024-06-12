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

select partman.create_parent(
    p_parent_table := 'public.event',
    p_control := 'id',
    p_type := 'native',
    p_interval := '5000',
    p_premake := 1
);

----

update partman.part_config
   set infinite_time_partitions=true
 where parent_table in ('event');
