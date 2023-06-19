create table post (
    id serial primary key,
    name text,
    link text UNIQUE,
    created timestamp,
    text text
);