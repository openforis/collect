CREATE TABLE "ofc_application_info"  ( 
	"version"	varchar(25) NOT NULL 
);
CREATE TABLE "ofc_config"  ( 
	"name" 	varchar(25) NOT NULL PRIMARY KEY,
	"value"	varchar(255) NOT NULL
);
CREATE TABLE "ofc_logo"  ( 
	"pos"  	integer NOT NULL,
	"image"	bytea NOT NULL,
	PRIMARY KEY("pos")
);
CREATE TABLE "ofc_record"  ( 
	"id"                       	integer PRIMARY KEY AUTOINCREMENT,
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
	FOREIGN KEY("survey_id") REFERENCES "collect"."ofc_survey"("id"),
	FOREIGN KEY("created_by_id") REFERENCES "collect"."ofc_user"("id"),
	FOREIGN KEY("modified_by_id") REFERENCES "collect"."ofc_user"("id")
);
CREATE TABLE "ofc_sampling_design"  ( 
	"id"            	integer  PRIMARY KEY AUTOINCREMENT,
	"survey_id"     	integer NULL,
	"survey_work_id"	integer NULL,
	"level1"       		varchar(256) NOT NULL,
	"level2"       		varchar(256) NULL,
	"level3"       		varchar(256) NULL,
	"location"      	varchar(256) NOT NULL,
	FOREIGN KEY("survey_id") REFERENCES "collect"."ofc_survey"("id"),
	FOREIGN KEY("survey_work_id") REFERENCES "collect"."ofc_survey_work"("id")
);
CREATE TABLE "ofc_survey"  ( 
	"id"  	integer PRIMARY KEY AUTOINCREMENT,
	"name"	varchar(255) NOT NULL UNIQUE,
	"uri" 	varchar(255) NOT NULL UNIQUE,
	"idml"	text NOT NULL
);
CREATE TABLE "ofc_survey_work"  ( 
	"id"  	integer PRIMARY KEY AUTOINCREMENT,
	"name"	varchar(255) NOT NULL UNIQUE,
	"uri" 	varchar(255) NOT NULL UNIQUE,
	"idml"	text NOT NULL
);
CREATE TABLE "ofc_taxon"  ( 
	"id"             	integer PRIMARY KEY AUTOINCREMENT,
	"taxon_id"       	integer NULL,
	"code"           	varchar(32) NULL,
	"scientific_name"	varchar(255) NOT NULL,
	"taxon_rank"     	varchar(128) NOT NULL,
	"taxonomy_id"    	integer NOT NULL,
	"step"           	integer NOT NULL,
	"parent_id"      	integer NULL,
	FOREIGN KEY("parent_id") REFERENCES "collect"."ofc_taxon"("id"),
	FOREIGN KEY("taxonomy_id") REFERENCES "collect"."ofc_taxonomy"("id")	
);
CREATE TABLE "ofc_taxon_vernacular_name"  ( 
	"id"              	integer PRIMARY KEY AUTOINCREMENT,
	"vernacular_name" 	varchar(255) NULL,
	"language_code"   	varchar(3) NOT NULL,
	"language_variety"	varchar(255) NULL,
	"taxon_id"        	integer NULL,
	"step"            	integer NOT NULL,
	"qualifier1"	varchar(255) NULL,
    "qualifier2"	varchar(255) NULL,
    "qualifier3"	varchar(255) NULL,
	FOREIGN KEY("taxon_id") REFERENCES "collect"."ofc_taxon"("id")    
);
CREATE TABLE "ofc_taxonomy"  ( 
	"id"            	integer PRIMARY KEY AUTOINCREMENT,
	"name"          	varchar(255) NOT NULL UNIQUE,
	"metadata"      	text NOT NULL,
	"survey_id"     	integer NULL,
	"survey_work_id"	integer NULL,
	FOREIGN KEY("survey_id") REFERENCES "ofc_survey"("id"),
	FOREIGN KEY("survey_work_id") REFERENCES "collect"."ofc_survey_work"("id")
);
CREATE TABLE "ofc_user"  ( 
	"id"      	integer PRIMARY KEY AUTOINCREMENT,
	"username"	varchar(255) NOT NULL,
	"password"	varchar(255) NOT NULL,
	"enabled" 	char(1) NOT NULL DEFAULT 'Y'
);
CREATE TABLE "ofc_user_role"  ( 
	"id"     	integer PRIMARY KEY AUTOINCREMENT,
	"user_id"	integer NOT NULL,
	"role"   	varchar(256) NULL,
	FOREIGN KEY("user_id") REFERENCES "collect"."ofc_user"("id")	
);

CREATE UNIQUE INDEX "ofc_taxon_id_key"
	ON "ofc_taxon"("taxon_id", "taxonomy_id");
	
CREATE UNIQUE INDEX "ofc_taxonomy_name_work_key"
	ON "ofc_taxonomy"("survey_work_id", "name");
