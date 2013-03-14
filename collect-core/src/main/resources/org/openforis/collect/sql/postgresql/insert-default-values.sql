INSERT INTO "collect"."ofc_application_info" ("version")
VALUES ('3.0-Alpha5');

INSERT INTO "collect"."ofc_config" ("name", "value") VALUES 
	('upload_path', '/home/openforis/collect-upload'); 
	-- ('index_path', '/home/openforis/collect-index');

INSERT INTO collect.ofc_user(id, username, password ,enabled)
VALUES (nextval('collect.ofc_user_id_seq'), 'admin', '21232f297a57a5a743894a0e4a801fc3', 'Y');

INSERT INTO collect.ofc_user_role(id, user_id, role)
VALUES (nextval('collect.ofc_user_role_id_seq'), currval('collect.ofc_user_id_seq'), 'ROLE_ADMIN');
