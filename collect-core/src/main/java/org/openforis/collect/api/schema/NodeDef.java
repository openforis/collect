package org.openforis.collect.api.schema;

public abstract class NodeDef {
    public final String id;
    public final String label;

    public NodeDef(String id, String label) {
        this.id = id;
        this.label = label;
    }
}
