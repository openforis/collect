CREATE SCHEMA "collect";
CREATE SEQUENCE "collect"."ofc_record_id_seq";
CREATE SEQUENCE "collect"."ofc_sampling_design_id_seq";
CREATE SEQUENCE "collect"."ofc_survey_id_seq";
CREATE SEQUENCE "collect"."ofc_survey_work_id_seq";
CREATE SEQUENCE "collect"."ofc_taxonomy_id_seq";
CREATE SEQUENCE "collect"."ofc_taxon_id_seq";
CREATE SEQUENCE "collect"."ofc_taxon_vernacular_name_id_seq";
CREATE SEQUENCE "collect"."ofc_user_id_seq"	;
CREATE SEQUENCE "collect"."ofc_user_role_id_seq";

CREATE TABLE "collect"."ofc_application_info"  ( 
	"version"	varchar(25) NOT NULL 
);
CREATE TABLE "collect"."ofc_config"  ( 
	"name" 	varchar(25) NOT NULL,
	"value"	varchar(255) NOT NULL,
	PRIMARY KEY("name")
);
CREATE TABLE "collect"."ofc_logo"  ( 
	"pos"  	integer NOT NULL,
	"image"	bytea NOT NULL,
	PRIMARY KEY("pos")
);
CREATE TABLE "collect"."ofc_record"  ( 
	"id"                       	integer NOT NULL,
	"survey_id"					integer NOT NULL,
	"root_entity_definition_id"	integer NOT NULL,
	"date_created"             	timestamp NULL,
	"created_by_id"            	integer NULL,
	"date_modified"            	timestamp NULL,
	"modified_by_id"           	integer NULL,
	"model_version"            	varchar(255) NULL,
	"step"                     	integer NULL,
	"state"                    	char(1) NULL,
	"skipped"                  	integer NULL,
	"missing"                  	integer NULL,
	"errors"                   	integer NULL,
	"warnings"                 	integer NULL,
	"key1"                     	varchar(2048) NULL,
	"key2"                     	varchar(2048) NULL,
	"key3"                     	varchar(2048) NULL,
	"count1"                   	integer NULL,
	"count2"                   	integer NULL,
	"count3"                   	integer NULL,
	"count4"                   	integer NULL,
	"count5"                   	integer NULL,
	"data1"                    	bytea NULL,
	"data2"                    	bytea NULL,
	PRIMARY KEY("id")
);
CREATE TABLE "collect"."ofc_sampling_design"  ( 
	"id"            	integer NOT NULL,
	"survey_id"     	integer NULL,
	"survey_work_id"	integer NULL,
	"level1"       		varchar(255) NOT NULL,
	"level2"       		varchar(255) NULL,
	"level3"       		varchar(255) NULL,
	"location"      	varchar(255) NOT NULL,
	PRIMARY KEY("id")
);
CREATE TABLE "collect"."ofc_survey"  ( 
	"id"  	integer NOT NULL,
	"name"	varchar(255) NOT NULL,
	"uri" 	varchar(255) NOT NULL,
	"idml"	text NOT NULL,
	PRIMARY KEY("id")
);
CREATE TABLE "collect"."ofc_survey_work"  ( 
	"id"  	integer NOT NULL,
	"name"	varchar(255) NOT NULL,
	"uri" 	varchar(255) NOT NULL,
	"idml"	text NOT NULL,
	PRIMARY KEY("id")
);
CREATE TABLE "collect"."ofc_taxon"  ( 
	"id"             	integer NOT NULL,
	"taxon_id"       	integer NULL,
	"code"           	varchar(32) NULL,
	"scientific_name"	varchar(255) NOT NULL,
	"taxon_rank"     	varchar(128) NOT NULL,
	"taxonomy_id"    	integer NOT NULL,
	"step"           	integer NOT NULL,
	"parent_id"      	integer NULL,
	PRIMARY KEY("id")
);
CREATE TABLE "collect"."ofc_taxon_vernacular_name"  ( 
	"id"              	integer NOT NULL,
	"vernacular_name" 	varchar(255) NULL,
	"language_code"   	varchar(3) NOT NULL,
	"language_variety"	varchar(255) NULL,
	"taxon_id"        	integer NULL,
	"step"            	integer NOT NULL,
	"qualifier1"	varchar(255) NULL,
    "qualifier2"	varchar(255) NULL,
    "qualifier3"	varchar(255) NULL,
	PRIMARY KEY("id")
);
COMMENT ON COLUMN "collect"."ofc_taxon_vernacular_name"."language_variety" IS 'Dialect, lect, sublanguage or other';
CREATE TABLE "collect"."ofc_taxonomy"  ( 
	"id"            	integer NOT NULL,
	"name"          	varchar(255) NOT NULL,
	"metadata"      	text NOT NULL,
	"survey_id"     	integer NULL,
	"survey_work_id"	integer NULL,
	PRIMARY KEY("id")
);
CREATE TABLE "collect"."ofc_user"  ( 
	"id"      	integer NOT NULL,
	"username"	varchar(255) NOT NULL,
	"password"	varchar(255) NOT NULL,
	"enabled" 	char(1) NOT NULL DEFAULT 'Y',
	PRIMARY KEY("id")
);
CREATE TABLE "collect"."ofc_user_role"  ( 
	"id"     	integer NOT NULL,
	"user_id"	integer NOT NULL,
	"role"   	varchar(256) NULL,
	PRIMARY KEY("id")
);
ALTER TABLE "collect"."ofc_survey"
	ADD CONSTRAINT "ofc_survey_name_key"
	UNIQUE ("name");
ALTER TABLE "collect"."ofc_survey"
	ADD CONSTRAINT "ofc_survey_uri_key"
	UNIQUE ("uri");
ALTER TABLE "collect"."ofc_survey_work"
	ADD CONSTRAINT "ofc_survey_work_name_key"
	UNIQUE ("name");
ALTER TABLE "collect"."ofc_survey_work"
	ADD CONSTRAINT "ofc_survey_work_uri_key"
	UNIQUE ("uri");
ALTER TABLE "collect"."ofc_taxon"
	ADD CONSTRAINT "ofc_taxon_id_key"
	UNIQUE ("taxon_id", "taxonomy_id");
ALTER TABLE "collect"."ofc_taxonomy"
	ADD CONSTRAINT "ofc_taxonomy_survey_fkey"
	FOREIGN KEY("survey_id")
	REFERENCES "collect"."ofc_survey"("id");
ALTER TABLE "collect"."ofc_taxonomy"
	ADD CONSTRAINT "ofc_taxonomy_survey_work_fkey"
	FOREIGN KEY("survey_work_id")
	REFERENCES "collect"."ofc_survey_work"("id");
ALTER TABLE "collect"."ofc_taxonomy"
	ADD CONSTRAINT "ofc_taxonomy_name_key"
	UNIQUE ("survey_id", "name");
ALTER TABLE "collect"."ofc_taxonomy"
	ADD CONSTRAINT "ofc_taxonomy_name_work_key"
	UNIQUE ("survey_work_id", "name");
ALTER TABLE "collect"."ofc_taxon_vernacular_name"
	ADD CONSTRAINT "ofc_taxon_vernacular_name_taxon_fkey"
	FOREIGN KEY("taxon_id")
	REFERENCES "collect"."ofc_taxon"("id");
ALTER TABLE "collect"."ofc_taxon"
	ADD CONSTRAINT "ofc_taxon_parent_fkey"
	FOREIGN KEY("parent_id")
	REFERENCES "collect"."ofc_taxon"("id");
ALTER TABLE "collect"."ofc_taxon"
	ADD CONSTRAINT "ofc_taxon_taxonomy_fkey"
	FOREIGN KEY("taxonomy_id")
	REFERENCES "collect"."ofc_taxonomy"("id");
ALTER TABLE "collect"."ofc_user_role"
	ADD CONSTRAINT "ofc_user_user_role_fkey"
	FOREIGN KEY("user_id")
	REFERENCES "collect"."ofc_user"("id");
ALTER TABLE "collect"."ofc_record"
  ADD CONSTRAINT "ofc_record_survey_fkey"
	FOREIGN KEY("survey_id")
	REFERENCES "collect"."ofc_survey"("id");
ALTER TABLE "collect"."ofc_record"
	ADD CONSTRAINT "ofc_record_created_by_user_fkey"
	FOREIGN KEY("created_by_id")
	REFERENCES "collect"."ofc_user"("id");
ALTER TABLE "collect"."ofc_record"
	ADD CONSTRAINT "ofc_record_modified_by_user_fkey"
	FOREIGN KEY("modified_by_id")
	REFERENCES "collect"."ofc_user"("id");
ALTER TABLE "collect"."ofc_sampling_design"
	ADD CONSTRAINT "ofc_sampling_design_survey_fkey"
	FOREIGN KEY("survey_id")
	REFERENCES "collect"."ofc_survey"("id");
ALTER TABLE "collect"."ofc_sampling_design"
	ADD CONSTRAINT "ofc_sampling_design_survey_work_fkey"
	FOREIGN KEY("survey_work_id")
	REFERENCES "collect"."ofc_survey_work"("id");
ALTER TABLE "collect"."ofc_user"
	ADD CONSTRAINT "ofc_user_username_key"
	UNIQUE ("username");

----------------------------
--- APPLICATION VERSION
----------------------------
INSERT INTO "collect"."ofc_application_info" ("version") VALUES ('3.0-Alpha6');

-- INSERT INTO "collect"."ofc_config" ("name", "value") VALUES 
--	('upload_path', '/home/openforis/collect-upload'),
--  ('index_path', '/home/openforis/collect-index');

----------------------------
--- ADMIN USER
----------------------------
INSERT INTO collect.ofc_user(id, username, password ,enabled) VALUES 
	(nextval('collect.ofc_user_id_seq'), 'admin', '21232f297a57a5a743894a0e4a801fc3', 'Y');

INSERT INTO collect.ofc_user_role(id, user_id, role) VALUES 
	(nextval('collect.ofc_user_role_id_seq'), currval('collect.ofc_user_id_seq'), 'ROLE_ADMIN');
