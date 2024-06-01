psql -U event << EOF
\c event;

create schema if not exists partman;
create extension pg_partman schema partman;
grant all on schema partman to public;
grant all on all tables in schema partman to public;
grant execute on all functions in schema partman to public;
grant execute on all procedures in schema partman to public;

EOF
