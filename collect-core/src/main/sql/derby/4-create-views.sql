CREATE VIEW "collect"."node_count_view"
    ("record_id", "definition_id", "node_count") 
AS
    SELECT "record_id", "definition_id", count("id")
    FROM "collect"."data"
    GROUP BY "record_id", "definition_id"
GO