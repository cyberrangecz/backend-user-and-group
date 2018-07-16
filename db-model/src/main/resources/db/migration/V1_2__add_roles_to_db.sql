-- inserting main roles
INSERT INTO role (role_type) VALUES ('ADMINISTRATOR');
INSERT INTO role (role_type) VALUES ('USER');
INSERT INTO role (role_type) VALUES ('GUEST');

-- inserting groups for main roles
INSERT INTO idm_group (name, status, description) VALUES ('ADMINISTRATOR', 'VALID', 'Initial group for users with ADMINISTRATOR role');
INSERT INTO idm_group (name, status, description) VALUES ('USER', 'VALID', 'Initial group for users with USER role');
INSERT INTO idm_group (name, status, description) VALUES ('GUEST', 'VALID', 'Initial group for users with GUEST role');

-- connecting main roles with groups
INSERT INTO idm_group_role (role_id, idm_group_id) VALUES ((SELECT id FROM role WHERE role_type = 'ADMINISTRATOR'), (SELECT id FROM idm_group WHERE name = 'ADMINISTRATOR'));
INSERT INTO idm_group_role (role_id, idm_group_id) VALUES ((SELECT id FROM role WHERE role_type = 'USER'), (SELECT id FROM idm_group WHERE name = 'USER'));
INSERT INTO idm_group_role (role_id, idm_group_id) VALUES ((SELECT id FROM role WHERE role_type = 'GUEST'), (SELECT id FROM idm_group WHERE name = 'GUEST'));