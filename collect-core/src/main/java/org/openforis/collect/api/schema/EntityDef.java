package org.openforis.collect.api.schema;

import java.util.List;

public class EntityDef extends NodeDef {
    public final List<? extends NodeDef> memberDefs;

    public EntityDef(String id, String label, List<? extends NodeDef> memberDefs) {
        super(id, label);
        this.memberDefs = memberDefs;
    }
}
