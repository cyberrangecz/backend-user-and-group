CREATE TABLE users (
    id          bigserial    NOT NULL,
    external_id int8 UNIQUE,
    family_name varchar(255),
    full_name   varchar(255),
    given_name  varchar(255),
    iss         varchar(255) NOT NULL UNIQUE,
    mail        varchar(255),
    picture     oid,
    status      varchar(255),
    sub         varchar(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE microservice (
    id       bigserial    NOT NULL,
    endpoint varchar(255) NOT NULL,
    name     varchar(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE role (
    id              bigserial    NOT NULL,
    description     varchar(255),
    role_type       varchar(255) NOT NULL UNIQUE,
    microservice_id int8         NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (microservice_id) REFERENCES microservice
);

CREATE TABLE idm_group (
    id              bigserial    NOT NULL,
    description     varchar(255) NOT NULL,
    expiration_date timestamp,
    external_id     int8 UNIQUE,
    name            varchar(255) NOT NULL UNIQUE,
    status          varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE idm_group_role (
    idm_group_id int8 NOT NULL,
    role_id      int8 NOT NULL,
    PRIMARY KEY (idm_group_id, role_id),
    FOREIGN KEY (idm_group_id) REFERENCES idm_group,
    FOREIGN KEY (role_id) REFERENCES role
);

CREATE TABLE user_idm_group (
    idm_group_id int8 NOT NULL,
    user_id      int8 NOT NULL,
    PRIMARY KEY (idm_group_id, user_id),
    FOREIGN KEY (idm_group_id) REFERENCES idm_group,
    FOREIGN KEY (user_id) REFERENCES users
);

CREATE INDEX user_sub_and_iss_index
    ON users (sub, iss);

CREATE INDEX user_family_name_index
    ON users (family_name);