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

TODO: UPDATE
 
--- END GENERATED CREATE TABLES ---

CREATE VIEW "collect"."node_count_view"
    ("record_id", "definition_id", "node_count") 
AS
    SELECT "record_id", "definition_id", count("id")
    FROM "collect"."data"
    GROUP BY "record_id", "definition_id"
GO
