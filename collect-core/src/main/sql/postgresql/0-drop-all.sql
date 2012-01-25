DROP SEQUENCE collect.data_id_seq 
GO

DROP SEQUENCE collect.record_id_seq 
GO

DROP SEQUENCE collect.schema_definition_id_seq 
GO

DROP SEQUENCE collect.survey_id_seq 
GO

DROP SEQUENCE collect.taxonomy_id_seq 
GO

DROP SEQUENCE collect.taxon_id_seq 
GO

DROP SEQUENCE collect.taxon_name_id_seq 
GO

DROP SEQUENCE collect.user_id_seq	 
GO

DROP SEQUENCE collect.user_role_id_seq 
GO

DROP SCHEMA collect 
GO

ALTER TABLE collect.data
	DROP CONSTRAINT FK_data_parent CASCADE 
GO
ALTER TABLE collect.data
	DROP CONSTRAINT FK_data_record CASCADE 
GO
ALTER TABLE collect.data
	DROP CONSTRAINT FK_data_schema_definition CASCADE 
GO
ALTER TABLE collect.record
	DROP CONSTRAINT FK_record_root_entity CASCADE 
GO
ALTER TABLE collect.schema_definition
	DROP CONSTRAINT FK_schema_definition_survey CASCADE 
GO
ALTER TABLE collect.taxon_name
	DROP CONSTRAINT FK_taxon_name_taxon CASCADE 
GO
ALTER TABLE collect.data
	DROP CONSTRAINT FK_data_taxon CASCADE 
GO
ALTER TABLE collect.taxon
	DROP CONSTRAINT FK_taxon_taxonomy CASCADE 
GO
ALTER TABLE collect.user_role
	DROP CONSTRAINT FK_user_user_role CASCADE 
GO
ALTER TABLE collect.record
	DROP CONSTRAINT FK_record_locked_by_user CASCADE 
GO
ALTER TABLE collect.record
	DROP CONSTRAINT FK_record_created_by_user CASCADE 
GO
ALTER TABLE collect.record
	DROP CONSTRAINT FK_record_modified_by_user CASCADE 
GO
ALTER TABLE collect.survey
	DROP CONSTRAINT UK_survey_name CASCADE 
GO
ALTER TABLE collect.survey
	DROP CONSTRAINT UK_survey_uri CASCADE 
GO

DROP VIEW collect.node_count_view
GO

-- DROP TABLES --

DROP TABLE IF EXISTS collect.data
GO
DROP TABLE IF EXISTS collect.logo
GO
DROP TABLE IF EXISTS collect.record
GO
DROP TABLE IF EXISTS collect.schema_definition
GO
DROP TABLE IF EXISTS collect.survey
GO
DROP TABLE IF EXISTS collect.taxon
GO
DROP TABLE IF EXISTS collect.taxon_name
GO
DROP TABLE IF EXISTS collect.taxonomy
GO
DROP TABLE IF EXISTS collect.user_account
GO
DROP TABLE IF EXISTS collect.user_role
GO

DROP SCHEMA collect
GO