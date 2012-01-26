CREATE TABLE "collect"."data"  ( 
	"id"           	INTEGER NOT NULL,
	"record_id"    	INTEGER NOT NULL,
	"definition_id"	INTEGER NOT NULL,
	"number1"      	DECIMAL(15,5),
	"number2"      	DECIMAL(15,5),
	"number3"      	DECIMAL(15,5),
	"text1"        	VARCHAR(2048),
	"text2"        	VARCHAR(2048),
	"text3"        	VARCHAR(2048),
	"taxon_id"     	INTEGER,
	"remarks"      	VARCHAR(2048),
	"symbol"       	CHAR(1),
	"state"        	CHAR(1),
	"parent_id"    	INTEGER,
	"idx"          	INTEGER NOT NULL,
	CONSTRAINT "PK_data" PRIMARY KEY("id")
)
GO
CREATE TABLE "collect"."logo"  ( 
	"pos"  	INTEGER NOT NULL,
	"image"	BLOB(2147483647) NOT NULL,
	CONSTRAINT "PK_logo" PRIMARY KEY("pos")
)
GO
CREATE TABLE "collect"."record"  ( 
	"id"            	INTEGER NOT NULL,
	"root_entity_id"	INTEGER NOT NULL,
	"date_created"  	TIMESTAMP,
	"created_by_id" 	INTEGER,
	"date_modified" 	TIMESTAMP,
	"modified_by_id"	INTEGER,
	"model_version" 	VARCHAR(255) NOT NULL,
	"step"          	INTEGER,
	"state"         	CHAR(1),
	"locked_by_id"  	INTEGER,
	"skipped"       	INTEGER,
	"missing"       	INTEGER,
	"errors"        	INTEGER,
	"warnings"      	INTEGER,
	"key_number1"   	DECIMAL(15,5) NULL,
	"key_number2"   	DECIMAL(15,5) NULL,
	"key_number3"   	DECIMAL(15,5) NULL,
	"key_text1"     	VARCHAR(2048) NULL,
	"key_text2"     	VARCHAR(2048) NULL,
	"key_text3"     	VARCHAR(2048) NULL,
	"count1"        	INTEGER NULL,
	"count2"        	INTEGER NULL,
	"count3"        	INTEGER NULL,
	"count4"        	INTEGER NULL,
	"count5"        	INTEGER NULL,
	CONSTRAINT "PK_record" PRIMARY KEY("id")
)
GO
CREATE TABLE "collect"."schema_definition"  ( 
	"id"       	INTEGER NOT NULL,
	"survey_id"	INTEGER NOT NULL,
	"path"     	VARCHAR(255) NOT NULL,
	CONSTRAINT "PK_schema_definition" PRIMARY KEY("id")
)
GO
CREATE TABLE "collect"."survey"  ( 
	"id"  	INTEGER NOT NULL,
	"name"	VARCHAR(255) NOT NULL,
	"uri" 	VARCHAR(255),
	"idml"	CLOB(1073741825) NOT NULL,
	CONSTRAINT "PK_survey" PRIMARY KEY("id")
)
GO
CREATE TABLE "collect"."taxon"  ( 
	"id"             	INTEGER NOT NULL,
	"scientific_name"	VARCHAR(255) NOT NULL,
	"level"          	INTEGER NOT NULL,
	"taxonomy_id"    	INTEGER NOT NULL,
	"step"           	INTEGER NOT NULL,
	CONSTRAINT "PK_taxon" PRIMARY KEY("id")
)
GO
CREATE TABLE "collect"."taxon_name"  ( 
	"id"              	INTEGER NOT NULL,
	"name"            	VARCHAR(255),
	"language_code"   	CHAR(3) NOT NULL,
	"language_variety"	VARCHAR(255),
	"taxon_id"        	INTEGER,
	"step"            	INTEGER NOT NULL,
	CONSTRAINT "PK_taxon_name" PRIMARY KEY("id")
)
GO
CREATE TABLE "collect"."taxonomy"  ( 
	"id"      	INTEGER NOT NULL,
	"name"    	VARCHAR(255) NOT NULL,
	"metadata"	CLOB(1073741825) NOT NULL,
	CONSTRAINT "PK_taxonomy" PRIMARY KEY("id")
)
GO
CREATE TABLE "collect"."user_account"  ( 
	"id"      	INTEGER NOT NULL,
	"username"	VARCHAR(255) NOT NULL,
	"password"	VARCHAR(255) NOT NULL,
	"enabled" 	CHAR(1) NOT NULL DEFAULT 'Y',
	CONSTRAINT "PK_user" PRIMARY KEY("id")
)
GO
CREATE TABLE "collect"."user_role"  ( 
	"id"     	INTEGER NOT NULL,
	"user_id"	INTEGER NOT NULL,
	"role"   	VARCHAR(256),
	CONSTRAINT "PK_user_role" PRIMARY KEY("id")
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
