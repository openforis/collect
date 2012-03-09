
INSERT INTO collect.ofc_user(id, username, password, enabled) 
	VALUES(1,'eko',md5('eko'),'Y')
GO

INSERT INTO collect.ofc_user_role(id, user_id, role) 
	VALUES(1, 1, 'ROLE_ENTRY')
GO

