package org.openforis.collect.js;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openforis.collect.api.schema.AttributeDef;
import org.openforis.collect.api.schema.EntityDef;
import org.openforis.collect.api.schema.EntityListDef;
import org.openforis.collect.api.schema.NodeDef;

public class SchemaJsonSerializer {
    String serialize(EntityDef schema) {
        JsonObject json = entity(schema, new JsonObject());
        return new GsonBuilder().setPrettyPrinting().create().toJson(json);
    }

    private JsonObject node(NodeDef node, JsonObject json) {
        if (node instanceof EntityDef)
            return entity((EntityDef) node, json);
        if (node instanceof AttributeDef)
            return attribute((AttributeDef) node, json);
        if (node instanceof EntityListDef)
            return entityList((EntityListDef) node, json);
        throw new IllegalStateException("Encountered unknown node definition type: " + node.getClass().getName());
    }

    private JsonObject entity(EntityDef entity, JsonObject json) {
        json.addProperty("type", "Entity");
        commonNodeAttributes(entity, json);
        JsonArray memberJson = new JsonArray();
        for (NodeDef memberDef : entity.memberDefs)
            memberJson.add(node(memberDef, new JsonObject()));
        json.add("members", memberJson);
        return json;
    }

    private JsonObject attribute(AttributeDef attribute, JsonObject json) {
        json.addProperty("type", "Attribute");
        commonNodeAttributes(attribute, json);
        json.addProperty("valueType", attribute.valueType.name());
        return json;
    }

    private JsonObject entityList(EntityListDef entityList, JsonObject json) {
        json.addProperty("type", "EntityList");
        commonNodeAttributes(entityList, json);
        json.add("member", node(entityList.memberDef, new JsonObject()));
        return json;
    }

    private JsonObject commonNodeAttributes(NodeDef node, JsonObject json) {
        json.addProperty("id", node.id);
        json.addProperty("label", node.label);
        return json;
    }
}
