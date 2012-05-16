
ALTER TABLE collect.ofc_record
	ADD COLUMN root_entity_definition_id integer 
GO
ALTER TABLE "collect"."ofc_record"
	ADD CONSTRAINT "ofc_record_root_entity_definition_fkey"
	FOREIGN KEY("root_entity_definition_id")
	REFERENCES "collect"."ofc_schema_definition"("id")
GO

CREATE TABLE "collect"."ofc_config"  ( 
	"name" 	varchar(25) NOT NULL,
	"value"	varchar(255) NOT NULL,
	PRIMARY KEY("name")
)
GO

INSERT INTO "collect"."ofc_config" ("name", "value") VALUES ('upload_path', '/home/openforis/collect-upload')
GO
       	
ALTER TABLE collect.ofc_taxon
	ADD COLUMN "taxon_id" integer
GO

ALTER TABLE collect.ofc_taxon_vernacular_name
	ADD COLUMN "qualifier1" varchar(32)
GO

ALTER TABLE collect.ofc_taxon_vernacular_name
	ADD COLUMN "qualifier2" varchar(32)
GO

ALTER TABLE collect.ofc_taxon_vernacular_name
	ADD COLUMN "qualifier3" varchar(32)
GO

UPDATE collect.ofc_record 
	SET  root_entity_definition_id=1447
GO


ALTER TABLE collect.ofc_record
	 ALTER COLUMN root_entity_definition_id set not null
GO

ALTER TABLE collect.ofc_record
	ADD COLUMN "lock_id" varchar(50) NULL
GO
