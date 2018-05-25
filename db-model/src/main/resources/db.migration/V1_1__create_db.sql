CREATE TABLE users (
   id             BIGSERIAL NOT NULL,
   liferay_sn     varchar(255) NOT NULL UNIQUE,
   display_name   varchar(255),
   mail           varchar(255),
   status         varchar(255),
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
   status         varchar(255),
   PRIMARY KEY (id));

CREATE TABLE user_role (
   roleid      int8 NOT NULL,
   userid      int8 NOT NULL,
   PRIMARY KEY (roleid, userid));


ALTER TABLE user_idm_group ADD CONSTRAINT FKuser_idm_g172082 FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE user_idm_group ADD CONSTRAINT FKuser_idm_g351385 FOREIGN KEY (idm_group_id) REFERENCES idm_group (id);

ALTER TABLE user_role ADD CONSTRAINT FKuser_role432826 FOREIGN KEY (roleid) REFERENCES role (id);
ALTER TABLE user_role ADD CONSTRAINT FKuser_role954230 FOREIGN KEY (userid) REFERENCES users (id);


-- assign roles of a default admin account accorgind to Liferay roles: 
INSERT INTO users (display_name, mail, status, liferay_sn) VALUES ('KYPO LOCAL ADMIN', 'info@kypo.cz', 'VALID', 'kypo');

