CREATE TABLE replay (
    replay_uuid uuid primary key,
    uri text not null,
    name text not null,
    source text not null,
    model text not null,
    mapping text,
    rate numeric,
    publishing_date date
);