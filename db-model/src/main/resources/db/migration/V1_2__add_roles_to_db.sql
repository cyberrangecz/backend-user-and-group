-- inserting main roles
INSERT INTO role(role_type) VALUES ('ADMINISTRATOR');
INSERT INTO role(role_type) VALUES ('USER');
INSERT INTO role(role_type) VALUES ('GUEST');

-- adding hierarchy between roles above
INSERT INTO hierarchy_of_roles (parent_role_id, child_role_id) VALUES ((SELECT id FROM role WHERE role_type = 'ADMINISTRATOR'), (SELECT id FROM role WHERE role_type = 'USER'));
INSERT INTO hierarchy_of_roles (parent_role_id, child_role_id) VALUES ((SELECT id FROM role WHERE role_type = 'ADMINISTRATOR'), (SELECT id FROM role WHERE role_type = 'GUEST'));
INSERT INTO hierarchy_of_roles (parent_role_id, child_role_id) VALUES ((SELECT id FROM role WHERE role_type = 'USER'), (SELECT id FROM role WHERE role_type = 'GUEST'));