CREATE UNIQUE INDEX idm_group_name_index
ON idm_group (name);

CREATE UNIQUE INDEX microservice_name_index
ON microservice (name);

CREATE UNIQUE INDEX role_role_type_index
ON role (role_type);

CREATE UNIQUE INDEX user_login_index
ON users (login);

CREATE INDEX user_full_name_index
ON users (full_name);
