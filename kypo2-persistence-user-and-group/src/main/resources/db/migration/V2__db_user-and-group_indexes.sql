CREATE UNIQUE INDEX idm_group_name_index
ON idm_group (name);

CREATE UNIQUE INDEX microservice_name_index
ON microservice (name);

CREATE UNIQUE INDEX role_role_type_index
ON role (role_type);

CREATE INDEX user_sub_and_iss_index
ON users (sub, iss);

CREATE INDEX user_family_name_index
ON users (family_name);