CREATE EXTENSION IF NOT EXISTS unaccent WITH SCHEMA public;
CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;

CREATE FUNCTION search_fold(text) RETURNS text
    LANGUAGE sql IMMUTABLE PARALLEL SAFE STRICT
    AS $$ SELECT lower(public.unaccent('public.unaccent', $1)) $$;

CREATE INDEX users_sub_search_fold_index ON users USING gin (search_fold(sub) gin_trgm_ops);
CREATE INDEX users_full_name_search_fold_index ON users USING gin (search_fold(full_name) gin_trgm_ops);
CREATE INDEX users_given_name_search_fold_index ON users USING gin (search_fold(given_name) gin_trgm_ops);
CREATE INDEX users_family_name_search_fold_index ON users USING gin (search_fold(family_name) gin_trgm_ops);
CREATE INDEX users_mail_search_fold_index ON users USING gin (search_fold(mail) gin_trgm_ops);
CREATE INDEX users_iss_search_fold_index ON users USING gin (search_fold(iss) gin_trgm_ops);
CREATE INDEX idm_group_name_search_fold_index ON idm_group USING gin (search_fold(name) gin_trgm_ops);
CREATE INDEX idm_group_description_search_fold_index ON idm_group USING gin (search_fold(description) gin_trgm_ops);
CREATE INDEX role_role_type_search_fold_index ON role USING gin (search_fold(role_type) gin_trgm_ops);
CREATE INDEX role_description_search_fold_index ON role USING gin (search_fold(description) gin_trgm_ops);
CREATE INDEX microservice_name_search_fold_index ON microservice USING gin (search_fold(name) gin_trgm_ops);
CREATE INDEX microservice_endpoint_search_fold_index ON microservice USING gin (search_fold(endpoint) gin_trgm_ops);
