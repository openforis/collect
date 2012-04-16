DROP SEQUENCE "collect"."ofc_record_id_seq" 
GO

DROP SEQUENCE "collect"."ofc_schema_definition_id_seq" 
GO

DROP SEQUENCE "collect"."ofc_survey_id_seq" 
GO

DROP SEQUENCE "collect"."ofc_taxonomy_id_seq" 
GO

DROP SEQUENCE "collect"."ofc_taxon_id_seq" 
GO

DROP SEQUENCE "collect"."ofc_taxon_vernacular_name_id_seq" 
GO

DROP SEQUENCE "collect"."ofc_user_id_seq"	 
GO

DROP SEQUENCE "collect"."ofc_user_role_id_seq" 
GO

----------------------------
--- BEGIN GENERATED CODE ---
----------------------------

ALTER TABLE "collect"."ofc_record"
	DROP CONSTRAINT "record_submitted_record_fkey" CASCADE 
GO
ALTER TABLE "collect"."ofc_record"
	DROP CONSTRAINT "record_root_entity_definition_fkey" CASCADE 
GO
ALTER TABLE "collect"."ofc_schema_definition"
	DROP CONSTRAINT "schema_definition_survey_fkey" CASCADE 
GO
ALTER TABLE "collect"."ofc_taxon_vernacular_name"
	DROP CONSTRAINT "ofc_taxon_vernacular_name_taxon_fkey" CASCADE 
GO
ALTER TABLE "collect"."ofc_taxon"
	DROP CONSTRAINT "ofc_taxon_parent_fkey" CASCADE 
GO
ALTER TABLE "collect"."ofc_taxon"
	DROP CONSTRAINT "ofc_taxon_taxonomy_fkey" CASCADE 
GO
ALTER TABLE "collect"."ofc_user_role"
	DROP CONSTRAINT "ofc_user_user_role_fkey" CASCADE 
GO
ALTER TABLE "collect"."ofc_record"
	DROP CONSTRAINT "ofc_record_locked_by_user_fkey" CASCADE 
GO
ALTER TABLE "collect"."ofc_record"
	DROP CONSTRAINT "ofc_record_created_by_user_fkey" CASCADE 
GO
ALTER TABLE "collect"."ofc_record"
	DROP CONSTRAINT "ofc_record_modified_by_user_fkey" CASCADE 
GO
ALTER TABLE "collect"."ofc_survey"
	DROP CONSTRAINT "ofc_survey_name_key" CASCADE 
GO
ALTER TABLE "collect"."ofc_survey"
	DROP CONSTRAINT "ofc_survey_uri_key" CASCADE 
GO
ALTER TABLE "collect"."ofc_taxonomy"
	DROP CONSTRAINT "ofc_taxonomy_name_key" CASCADE 
GO
DROP TABLE IF EXISTS "collect"."ofc_logo"
GO
DROP TABLE IF EXISTS "collect"."ofc_record"
GO
DROP TABLE IF EXISTS "collect"."ofc_schema_definition"
GO
DROP TABLE IF EXISTS "collect"."ofc_survey"
GO
DROP TABLE IF EXISTS "collect"."ofc_taxon"
GO
DROP TABLE IF EXISTS "collect"."ofc_taxon_vernacular_name"
GO
DROP TABLE IF EXISTS "collect"."ofc_taxonomy"
GO
DROP TABLE IF EXISTS "collect"."ofc_user"
GO
DROP TABLE IF EXISTS "collect"."ofc_user_role"
GO

--------------------------
--- END GENERATED CODE ---
--------------------------

DROP SCHEMA "collect"
GO
