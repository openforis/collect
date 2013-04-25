DROP SEQUENCE "collect"."ofc_record_id_seq";
DROP SEQUENCE "collect"."ofc_sampling_design_id_seq";
DROP SEQUENCE "collect"."ofc_survey_id_seq";
DROP SEQUENCE "collect"."ofc_survey_work_id_seq";
DROP SEQUENCE "collect"."ofc_taxonomy_id_seq";
DROP SEQUENCE "collect"."ofc_taxon_id_seq";
DROP SEQUENCE "collect"."ofc_taxon_vernacular_name_id_seq";
DROP SEQUENCE "collect"."ofc_user_id_seq";
DROP SEQUENCE "collect"."ofc_user_role_id_seq";

----------------------------
--- BEGIN GENERATED CODE ---
----------------------------
ALTER TABLE "collect"."ofc_record"
	DROP CONSTRAINT "ofc_record_survey_fkey" CASCADE;
ALTER TABLE "collect"."ofc_sampling_design"
	DROP CONSTRAINT "ofc_sampling_design_survey_fkey" CASCADE;
ALTER TABLE "collect"."ofc_sampling_design"
	DROP CONSTRAINT "ofc_sampling_design_survey_work_fkey" CASCADE;
ALTER TABLE "collect"."ofc_taxon_vernacular_name"
	DROP CONSTRAINT "ofc_taxon_vernacular_name_taxon_fkey" CASCADE;
ALTER TABLE "collect"."ofc_taxon"
	DROP CONSTRAINT "ofc_taxon_parent_fkey" CASCADE;
ALTER TABLE "collect"."ofc_taxon"
	DROP CONSTRAINT "ofc_taxon_taxonomy_fkey" CASCADE;
ALTER TABLE "collect"."ofc_taxonomy"
	DROP CONSTRAINT "ofc_taxonomy_survey_fkey" CASCADE ;
ALTER TABLE "collect"."ofc_taxonomy"
	DROP CONSTRAINT "ofc_taxonomy_survey_work_fkey" CASCADE ;
ALTER TABLE "collect"."ofc_user_role"
	DROP CONSTRAINT "ofc_user_user_role_fkey" CASCADE;
ALTER TABLE "collect"."ofc_record"
	DROP CONSTRAINT "ofc_record_created_by_user_fkey" CASCADE;
ALTER TABLE "collect"."ofc_record"
	DROP CONSTRAINT "ofc_record_modified_by_user_fkey" CASCADE;
ALTER TABLE "collect"."ofc_survey_work"
	DROP CONSTRAINT "ofc_survey_work_name_key" CASCADE;
ALTER TABLE "collect"."ofc_survey_work"
	DROP CONSTRAINT "ofc_survey_work_uri_key" CASCADE;
ALTER TABLE "collect"."ofc_survey"
	DROP CONSTRAINT "ofc_survey_name_key" CASCADE;
ALTER TABLE "collect"."ofc_survey"
	DROP CONSTRAINT "ofc_survey_uri_key" CASCADE;
ALTER TABLE "collect"."ofc_taxon"
	DROP CONSTRAINT "ofc_taxon_id_key" CASCADE;
ALTER TABLE "collect"."ofc_taxonomy"
	DROP CONSTRAINT "ofc_taxonomy_name_key" CASCADE;
ALTER TABLE "collect"."ofc_taxonomy"
	DROP CONSTRAINT "ofc_taxonomy_name_work_key" CASCADE;
ALTER TABLE "collect"."ofc_user"
	DROP CONSTRAINT "ofc_user_username_key" CASCADE;

DROP TABLE IF EXISTS "collect"."ofc_application_info";
DROP TABLE IF EXISTS "collect"."ofc_config";
DROP TABLE IF EXISTS "collect"."ofc_logo";
DROP TABLE IF EXISTS "collect"."ofc_record";
DROP TABLE IF EXISTS "collect"."ofc_sampling_design";
DROP TABLE IF EXISTS "collect"."ofc_survey_work";
DROP TABLE IF EXISTS "collect"."ofc_survey";
DROP TABLE IF EXISTS "collect"."ofc_taxon";
DROP TABLE IF EXISTS "collect"."ofc_taxon_vernacular_name";
DROP TABLE IF EXISTS "collect"."ofc_taxonomy";
DROP TABLE IF EXISTS "collect"."ofc_user";
DROP TABLE IF EXISTS "collect"."ofc_user_role";

--------------------------
--- END GENERATED CODE ---
--------------------------

DROP SCHEMA "collect";
