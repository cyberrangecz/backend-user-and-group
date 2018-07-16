CREATE TABLE users (
   id             BIGSERIAL NOT NULL,
   screen_name    varchar(255) NOT NULL UNIQUE,
   full_name      varchar(255),
   mail           varchar(255),
   status         varchar(255) NOT NULL,
   external_id    int8 UNIQUE,
   PRIMARY KEY (id));

CREATE TABLE role (
   id          BIGSERIAL NOT NULL,
   role_type   varchar(255) NOT NULL UNIQUE,
   PRIMARY KEY (id));

CREATE TABLE user_idm_group (
   user_id        int8 NOT NULL,
   idm_group_id   int8 NOT NULL,
   PRIMARY KEY (user_id, idm_group_id));

CREATE TABLE idm_group (
   id             BIGSERIAL NOT NULL,
   name           varchar(255) NOT NULL,
   external_id    int8 UNIQUE,
   status         varchar(255) NOT NULL,
   description    varchar(4096) NOT NULL,
   PRIMARY KEY (id));

CREATE TABLE idm_group_role (
   role_id        int8 NOT NULL,
   idm_group_id   int8 NOT NULL,
   PRIMARY KEY (role_id, idm_group_id));

ALTER TABLE user_idm_group ADD CONSTRAINT FKuser_idm_g172082 FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE user_idm_group ADD CONSTRAINT FKuser_idm_g351385 FOREIGN KEY (idm_group_id) REFERENCES idm_group (id);

ALTER TABLE idm_group_role ADD CONSTRAINT FKidm_group_284474 FOREIGN KEY (role_id) REFERENCES role (id);
ALTER TABLE idm_group_role ADD CONSTRAINT FKidm_group_389301 FOREIGN KEY (idm_group_id) REFERENCES idm_group (id);
