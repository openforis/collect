PRAGMA foreign_keys=off;

CREATE TABLE _ofc_survey_new (
	"id" INTEGER NOT NULL,
	"name" TEXT NOT NULL,
	"uri" TEXT NOT NULL,
	"idml" TEXT NOT NULL,
	"target" varchar(5) NOT NULL DEFAULT 'CD',
	"date_created" timestamp,
	"date_modified" timestamp,
	"collect_version" varchar(55) NOT NULL DEFAULT '3.4.0',
	"temporary" bool NOT NULL DEFAULT 0,
	"published_id" INTEGER,
	"usergroup_id" INTEGER,
	"availability" char(1),
	"title" varchar(255),
	"langs" varchar(20),
	CONSTRAINT "ofc_survey_name_key" UNIQUE("name","temporary"),
	CONSTRAINT "ofc_survey_uri_key" UNIQUE("uri","temporary"),
	CONSTRAINT "ofc_survey_pkey" PRIMARY KEY("id"),
	FOREIGN KEY("usergroup_id") REFERENCES "ofc_usergroup"("id")
);
				
INSERT INTO _ofc_survey_new (
	"id", "name", "uri", "idml", "target", "date_created", "date_modified", 
	"collect_version", "temporary", "published_id", "usergroup_id", "availability",
	"title", "langs")
SELECT 
	"id", "name", "uri", "idml", "target", "date_created", "date_modified", 
	"collect_version", "temporary", "published_id", "usergroup_id", "availability",
	"title", "langs" 
FROM ofc_survey;
				
DROP TABLE ofc_survey;

ALTER TABLE _ofc_survey_new RENAME TO ofc_survey;

PRAGMA foreign_keys=on;