package org.openforis.collect.js;

import org.junit.Test;
import org.openforis.collect.api.schema.AttributeDef;
import org.openforis.collect.api.schema.EntityDef;
import org.openforis.collect.api.schema.EntityListDef;
import org.openforis.collect.api.schema.ValueType;

import static java.util.Arrays.asList;

public class SchemaJsonSerializerTest {
    @Test
    public void test() {
        EntityDef schema = new EntityDef("plot", "Plot", asList(
                new AttributeDef("plot_number", "Plot Number", ValueType.Number),
                new EntityListDef("trees", "Trees",
                        new EntityDef("tree", "Tree", asList(
                                new AttributeDef("tree_number", "Tree Number", ValueType.Number)
                        ))
                ))
        );

        String s = new SchemaJsonSerializer().serialize(schema);
        System.out.println(s);
        // TODO: Assert something
    }
}
