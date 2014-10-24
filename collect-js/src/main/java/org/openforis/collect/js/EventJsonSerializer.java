package org.openforis.collect.js;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openforis.collect.api.event.AttributeValuesChangedEvent;
import org.openforis.collect.api.event.EntityAddedEvent;
import org.openforis.collect.api.event.Event;
import org.openforis.idm.model.Value;

import java.util.List;
import java.util.Map;

public class EventJsonSerializer {
    String serialize(List<? extends Event> events) {
        JsonArray json = new JsonArray();
        for (Event event : events) {
            json.add(event(event, new JsonObject()));
        }
        return new GsonBuilder().setPrettyPrinting().create().toJson(json);
    }

    private JsonObject event(Event event, JsonObject json) {
        if (event instanceof EntityAddedEvent)
            return entityAdded((EntityAddedEvent) event, json);
        if (event instanceof AttributeValuesChangedEvent)
            return attributeValuesChanged((AttributeValuesChangedEvent) event, json);
        throw new IllegalStateException("Encountered unknown event type: " + event.getClass().getName());
    }

    private JsonObject entityAdded(EntityAddedEvent event, JsonObject json) {
        json.addProperty("type", "EntityAdded");
        json.addProperty("parentId", event.parentId);
        json.addProperty("id", event.id);
        return json;
    }

    private JsonObject attributeValuesChanged(AttributeValuesChangedEvent event, JsonObject json) {
        json.addProperty("type", "AttributeValuesChanged");
        JsonObject values = new JsonObject();
        for (Map.Entry<String, Value> entry : event.valuesByAttributeId.entrySet())
            value(entry.getKey(), entry.getValue(), values);
        json.add("values", values);
        return json;
    }

    private void value(String attributeId, Value value, JsonObject json) {
        json.addProperty(attributeId, value.toString()); // TODO: Depend on value type
    }
}
