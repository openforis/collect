package org.openforis.collect.js;

import org.junit.Test;
import org.openforis.collect.api.command.Command;
import org.openforis.collect.api.event.AttributeValuesChangedEvent;
import org.openforis.collect.api.event.EntityAddedEvent;
import org.openforis.idm.model.TextValue;
import org.openforis.idm.model.Value;

import java.util.HashMap;

import static java.util.Arrays.asList;

public class EventJsonSerializerTest {

    public static final Command ANY_COMMAND = null;

    @Test
    public void test() {
        String s = new EventJsonSerializer().serialize(asList(
                new EntityAddedEvent(ANY_COMMAND, null, "event id"),
                new AttributeValuesChangedEvent(ANY_COMMAND, new HashMap<String, Value>() {{
                    put("attribute id", new TextValue("Attribute text value"));
                }})
        ));
        System.out.println(s);
        // TODO: Assert something
    }
}
