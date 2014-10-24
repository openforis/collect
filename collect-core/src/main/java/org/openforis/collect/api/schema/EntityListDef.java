package org.openforis.collect.api.schema;

public class EntityListDef extends NodeDef {
    public final EntityDef memberDef;

    public EntityListDef(String id, String label, EntityDef memberDef) {
        super(id, label);
        this.memberDef = memberDef;
    }
}
