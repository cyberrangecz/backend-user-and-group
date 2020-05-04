create table idm_group (
   id  bigserial not null,
    description varchar(255) not null,
    expiration_date timestamp,
    external_id int8,
    name varchar(255) not null,
    status varchar(255) not null,
    primary key (id)
);

create table idm_group_role (
   idm_group_id int8 not null,
    role_id int8 not null,
    primary key (idm_group_id, role_id)
);

create table microservice (
   id  bigserial not null,
    endpoint varchar(255) not null,
    name varchar(255) not null,
    primary key (id)
);

create table role (
   id  bigserial not null,
    description varchar(255),
    role_type varchar(255) not null,
    microservice_id int8 not null,
    primary key (id)
);

create table user_idm_group (
   idm_group_id int8 not null,
    user_id int8 not null,
    primary key (idm_group_id, user_id)
);

create table users (
   id  bigserial not null,
    external_id int8,
    family_name varchar(255),
    full_name varchar(255),
    given_name varchar(255),
    iss varchar(255) not null,
    mail varchar(255),
    picture oid,
    status varchar(255),
    sub varchar(255) not null,
    primary key (id)
);

alter table idm_group
   add constraint UK_h475jn0081undrjow1eldsvin unique (external_id);

alter table idm_group
   add constraint UK_7j4dvjf43f8ay24sny8g62tln unique (name);

alter table microservice
   add constraint UK_9r423xk5gncdkch9bjil9t0o unique (name);

alter table role
   add constraint UK_8nhufvk7ufr23s4xoqglqtbdx unique (role_type);

alter table users
   add constraint UKtf7yqacmy6bcr5sul746pyat6 unique (sub, iss);

alter table users
   add constraint UK_cup9hom3h5cte4btcq935d0uu unique (external_id);

alter table idm_group_role
   add constraint FKiog03cjidd3okuw45q78j4vce
   foreign key (role_id)
   references role;

alter table idm_group_role
   add constraint FKk43vkq7c4b9bv1f2g0hduphl8
   foreign key (idm_group_id)
   references idm_group;

alter table role
   add constraint FKe17f2hch8etw3fwwuno7t6ok3
   foreign key (microservice_id)
   references microservice;

alter table user_idm_group
   add constraint FKob696dgqi5tyfsngy3lts9930
   foreign key (user_id)
   references users;

alter table user_idm_group
   add constraint FK6mwd6bpb54n4lsj90diu40c4p
   foreign key (idm_group_id)
   references idm_group;