create table users
(
    id           uuid         not null
        primary key,
    email        varchar(255) not null,
    first_name   varchar(255) not null,
    last_name    varchar(255) not null,
    birth_date   date         not null,
    address      varchar(255),
    phone_number varchar(20),
    password     varchar(255),
    role         varchar(255)
);