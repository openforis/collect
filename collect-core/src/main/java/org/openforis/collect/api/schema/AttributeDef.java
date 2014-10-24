package org.openforis.collect.api.schema;

public class AttributeDef extends NodeDef {
    public final ValueType valueType;

    public AttributeDef(String id, String label, ValueType valueType) {
        super(id, label);
        this.valueType = valueType;
    }
}
