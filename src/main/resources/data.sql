insert into AUTHORITIES (id, name) values (0,'ROLE_PROCESSES');
insert into AUTHORITIES (id, name) values (1,'ROLE_SUPPLIERS');
insert into AUTHORITIES (id, name) values (2,'ROLE_TASKS');
insert into AUTHORITIES (id, name) values (3,'ROLE_USERS');
insert into AUTHORITIES (id, name) values (4,'ROLE_PERSONAS');

insert into PERSONAS (id, name) values (0,'Administrator');
insert into PERSONAS (id, name) values (1,'Credit Controller');
insert into PERSONAS (id, name) values (2,'Supplier Management');

insert into PERSONA_AUTHORITY (persona_id, authority_id) values (0, 0);
insert into PERSONA_AUTHORITY (persona_id, authority_id) values (0, 3);
insert into PERSONA_AUTHORITY (persona_id, authority_id) values (0, 4);
insert into PERSONA_AUTHORITY (persona_id, authority_id) values (1, 1);
insert into PERSONA_AUTHORITY (persona_id, authority_id) values (1, 2);
insert into PERSONA_AUTHORITY (persona_id, authority_id) values (2, 1);



insert into USERS (username, default_persona_id, enabled, password) values ('cc', 1, 1, '{noop}cc');
insert into USERS (username, default_persona_id, enabled, password) values ('admin', 0, 1, '{noop}admin');

insert into USER_PERSONA (username, persona_id) values ('cc', 1);
insert into USER_PERSONA (username, persona_id) values ('cc', 2);
insert into USER_PERSONA (username, persona_id) values ('admin', 0);