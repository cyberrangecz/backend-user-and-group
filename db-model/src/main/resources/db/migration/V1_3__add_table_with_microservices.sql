CREATE TABLE service (
   id             BIGSERIAL NOT NULL,
   name           varchar(255) NOT NULL UNIQUE,
   endpoint       varchar(255) NOT NULL UNIQUE,
   PRIMARY KEY (id));