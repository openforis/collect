DROP SEQUENCE "collect"."data_id_seq" RESTRICT
GO

DROP SEQUENCE "collect"."record_id_seq" RESTRICT
GO

DROP SEQUENCE "collect"."schema_definition_id_seq" RESTRICT
GO

DROP SEQUENCE "collect"."survey_id_seq" RESTRICT
GO

DROP SEQUENCE "collect"."taxonomy_id_seq" RESTRICT
GO

DROP SEQUENCE "collect"."taxon_id_seq" RESTRICT
GO

DROP SEQUENCE "collect"."taxon_name_id_seq" RESTRICT
GO

DROP SEQUENCE "collect"."user_id_seq" RESTRICT
GO

DROP SEQUENCE "collect"."user_role_id_seq" RESTRICT
GO

DROP VIEW "collect"."node_count_view"
GO

-- START GENERATED --


ALTER TABLE "collect"."data"
	DROP CONSTRAINT "FK_data_parent"
GO
ALTER TABLE "collect"."data"
	DROP CONSTRAINT "FK_data_record"
GO
ALTER TABLE "collect"."data"
	DROP CONSTRAINT "FK_data_schema_definition"
GO
ALTER TABLE "collect"."record"
	DROP CONSTRAINT "FK_record_root_entity"
GO
ALTER TABLE "collect"."schema_definition"
	DROP CONSTRAINT "FK_schema_definition_survey"
GO
ALTER TABLE "collect"."taxon_name"
	DROP CONSTRAINT "FK_taxon_name_taxon"
GO
ALTER TABLE "collect"."data"
	DROP CONSTRAINT "FK_data_taxon"
GO
ALTER TABLE "collect"."taxon"
	DROP CONSTRAINT "FK_taxon_taxonomy"
GO
ALTER TABLE "collect"."user_role"
	DROP CONSTRAINT "FK_user_user_role"
GO
ALTER TABLE "collect"."record"
	DROP CONSTRAINT "FK_record_locked_by_user"
GO
ALTER TABLE "collect"."record"
	DROP CONSTRAINT "FK_record_created_by_user"
GO
ALTER TABLE "collect"."record"
	DROP CONSTRAINT "FK_record_modified_by_user"
GO
ALTER TABLE "collect"."survey"
	DROP CONSTRAINT "UK_survey_name"
GO
ALTER TABLE "collect"."survey"
	DROP CONSTRAINT "UK_survey_uri"
GO
DROP TABLE "collect"."data"
GO
DROP TABLE "collect"."logo"
GO
DROP TABLE "collect"."record"
GO
DROP TABLE "collect"."schema_definition"
GO
DROP TABLE "collect"."survey"
GO
DROP TABLE "collect"."taxon"
GO
DROP TABLE "collect"."taxon_name"
GO
DROP TABLE "collect"."taxonomy"
GO
DROP TABLE "collect"."user_account"
GO
DROP TABLE "collect"."user_role"
GO

-- END GENERATED

DROP SCHEMA "collect" RESTRICT
GO
