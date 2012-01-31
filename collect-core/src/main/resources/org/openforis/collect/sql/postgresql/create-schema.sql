CREATE SCHEMA "collect"
GO

CREATE SEQUENCE "collect"."data_id_seq"
GO

CREATE SEQUENCE "collect"."record_id_seq"
GO

CREATE SEQUENCE "collect"."schema_definition_id_seq"
GO

CREATE SEQUENCE "collect"."survey_id_seq"
GO

CREATE SEQUENCE "collect"."taxonomy_id_seq"
GO

CREATE SEQUENCE "collect"."taxon_id_seq"
GO

CREATE SEQUENCE "collect"."taxon_name_id_seq"
GO

CREATE SEQUENCE "collect"."user_id_seq"	
GO

CREATE SEQUENCE "collect"."user_role_id_seq"
GO

--- BEGIN GENERATED CREATE TABLES ---
CREATE TABLE "collect"."data"  ( 
	"id"           	integer NOT NULL,
	"record_id"    	integer NOT NULL,
	"definition_id"	integer NOT NULL,
	"number1"      	numeric(15,5) NULL,
	"number2"      	numeric(15,5) NULL,
	"number3"      	numeric(15,5) NULL,
	"text1"        	varchar(2048) NULL,
	"text2"        	varchar(2048) NULL,
	"text3"        	varchar(2048) NULL,
	"taxon_id"     	integer NULL,
	"remarks"      	varchar(2048) NULL,
	"symbol"       	char(1) NULL,
	"state"        	char(1) NULL,
	"parent_id"    	integer NULL,
	"idx"          	integer NOT NULL,
	PRIMARY KEY("id")
)
GO
CREATE TABLE "collect"."logo"  ( 
	"pos"  	integer NOT NULL,
	"image"	bytea NOT NULL,
	PRIMARY KEY("pos")
)
GO
CREATE TABLE "collect"."record"  ( 
	"id"            	integer NOT NULL,
	"root_entity_id"	integer NOT NULL,
	"date_created"  	timestamp NULL,
	"created_by_id" 	integer NULL,
	"date_modified" 	timestamp NULL,
	"modified_by_id"	integer NULL,
	"model_version" 	varchar(255) NOT NULL,
	"step"          	integer NULL,
	"state"         	char(1) NULL,
	"locked_by_id"  	integer NULL,
	"skipped"       	integer NULL,
	"missing"       	integer NULL,
	"errors"        	integer NULL,
	"warnings"      	integer NULL,
	"key_number1"   	numeric(15,5) NULL,
	"key_number2"   	numeric(15,5) NULL,
	"key_number3"   	numeric(15,5) NULL,
	"key_text1"     	varchar(2048) NULL,
	"key_text2"     	varchar(2048) NULL,
	"key_text3"     	varchar(2048) NULL,
	"count1"        	integer NULL,
	"count2"        	integer NULL,
	"count3"        	integer NULL,
	"count4"        	integer NULL,
	"count5"        	integer NULL,
	PRIMARY KEY("id")
)
GO
CREATE TABLE "collect"."schema_definition"  ( 
	"id"       	integer NOT NULL,
	"survey_id"	integer NOT NULL,
	"path"     	varchar(255) NOT NULL,
	PRIMARY KEY("id")
)
GO
CREATE TABLE "collect"."survey"  ( 
	"id"  	integer NOT NULL,
	"name"	varchar(255) NOT NULL,
	"uri" 	varchar(255) NULL,
	"idml"	text NOT NULL,
	PRIMARY KEY("id")
)
GO
CREATE TABLE "collect"."taxon"  ( 
	"id"             	integer NOT NULL,
	"scientific_name"	varchar(255) NOT NULL,
	"taxon_rank"     	varchar(128) NOT NULL,
	"taxonomy_id"    	integer NOT NULL,
	"step"           	integer NOT NULL,
	"parent_id"      	integer NULL,
	PRIMARY KEY("id")
)
GO
CREATE TABLE "collect"."taxon_name"  ( 
	"id"              	integer NOT NULL,
	"name"            	varchar(255) NULL,
	"language_code"   	char(3) NOT NULL,
	"language_variety"	varchar(255) NULL,
	"taxon_id"        	integer NULL,
	"step"            	integer NOT NULL,
	PRIMARY KEY("id")
)
GO
COMMENT ON COLUMN "collect"."taxon_name"."language_variety" IS 'Dialect, lect, sublanguage or other'
GO
CREATE TABLE "collect"."taxonomy"  ( 
	"id"      	integer NOT NULL,
	"name"    	varchar(255) NOT NULL,
	"metadata"	text NOT NULL,
	PRIMARY KEY("id")
)
GO
CREATE TABLE "collect"."user_account"  ( 
	"id"      	integer NOT NULL,
	"username"	varchar(255) NOT NULL,
	"password"	varchar(255) NOT NULL,
	"enabled" 	char(1) NOT NULL DEFAULT 'Y',
	PRIMARY KEY("id")
)
GO
CREATE TABLE "collect"."user_role"  ( 
	"id"     	integer NOT NULL,
	"user_id"	integer NOT NULL,
	"role"   	varchar(256) NULL,
	PRIMARY KEY("id")
)
GO
ALTER TABLE "collect"."survey"
	ADD CONSTRAINT "UK_survey_name"
	UNIQUE ("name")
GO
ALTER TABLE "collect"."survey"
	ADD CONSTRAINT "UK_survey_uri"
	UNIQUE ("uri")
GO
ALTER TABLE "collect"."data"
	ADD CONSTRAINT "FK_data_parent"
	FOREIGN KEY("parent_id")
	REFERENCES "collect"."data"("id")
GO
ALTER TABLE "collect"."data"
	ADD CONSTRAINT "FK_data_record"
	FOREIGN KEY("record_id")
	REFERENCES "collect"."record"("id")
GO
ALTER TABLE "collect"."data"
	ADD CONSTRAINT "FK_data_schema_definition"
	FOREIGN KEY("definition_id")
	REFERENCES "collect"."schema_definition"("id")
GO
ALTER TABLE "collect"."record"
	ADD CONSTRAINT "FK_record_root_entity"
	FOREIGN KEY("root_entity_id")
	REFERENCES "collect"."schema_definition"("id")
GO
ALTER TABLE "collect"."schema_definition"
	ADD CONSTRAINT "FK_schema_definition_survey"
	FOREIGN KEY("survey_id")
	REFERENCES "collect"."survey"("id")
GO
ALTER TABLE "collect"."taxon_name"
	ADD CONSTRAINT "FK_taxon_name_taxon"
	FOREIGN KEY("taxon_id")
	REFERENCES "collect"."taxon"("id")
GO
ALTER TABLE "collect"."data"
	ADD CONSTRAINT "FK_data_taxon"
	FOREIGN KEY("taxon_id")
	REFERENCES "collect"."taxon"("id")
GO
ALTER TABLE "collect"."taxon"
	ADD CONSTRAINT "FK_taxon_parent"
	FOREIGN KEY("parent_id")
	REFERENCES "collect"."taxon"("id")
GO
ALTER TABLE "collect"."taxon"
	ADD CONSTRAINT "FK_taxon_taxonomy"
	FOREIGN KEY("taxonomy_id")
	REFERENCES "collect"."taxonomy"("id")
GO
ALTER TABLE "collect"."user_role"
	ADD CONSTRAINT "FK_user_user_role"
	FOREIGN KEY("user_id")
	REFERENCES "collect"."user_account"("id")
GO
ALTER TABLE "collect"."record"
	ADD CONSTRAINT "FK_record_locked_by_user"
	FOREIGN KEY("locked_by_id")
	REFERENCES "collect"."user_account"("id")
GO
ALTER TABLE "collect"."record"
	ADD CONSTRAINT "FK_record_created_by_user"
	FOREIGN KEY("created_by_id")
	REFERENCES "collect"."user_account"("id")
GO
ALTER TABLE "collect"."record"
	ADD CONSTRAINT "FK_record_modified_by_user"
	FOREIGN KEY("modified_by_id")
	REFERENCES "collect"."user_account"("id")
GO

--- END GENERATED CREATE TABLES ---
