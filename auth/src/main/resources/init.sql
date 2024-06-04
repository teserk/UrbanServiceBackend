create table customers
(
    id UUID not null primary key,
    username varchar not null,
    password varchar not null
);

create table houses
(
    id UUID not null primary key,
    longitude DOUBLE PRECISION not null,
    latitude DOUBLE PRECISION not null
);

create table streets
(
    id UUID not null primary key,
    firsthouseid UUID not null,
    secondhouseid UUID not null,
    length DOUBLE PRECISION not null
)