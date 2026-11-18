
\timing on
begin;


create temporary table lab_vectors (
    id        bigint generated always as identity primary key,
    embedding vector(1536) not null
) on commit drop;

insert into lab_vectors (embedding)
select (select array_agg(random()::real) from generate_series(1, 1536) where g > 0)::vector
from generate_series(1, 5000) as g;

analyze lab_vectors;


explain analyze
select id from lab_vectors
order by embedding <=> (select embedding from lab_vectors where id = 1)
limit 5;


create index on lab_vectors using hnsw (embedding vector_cosine_ops);


explain analyze
select id from lab_vectors
order by embedding <=> (select embedding from lab_vectors where id = 1)
limit 5;

rollback;