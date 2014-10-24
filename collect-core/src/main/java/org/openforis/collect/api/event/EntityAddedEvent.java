package org.openforis.collect.api.event;

import org.openforis.collect.api.command.Command;

public class EntityAddedEvent extends Event {
    public final String parentId;
    public final String id;

    public EntityAddedEvent(Command trigger, String parentId, String id) {
        super(trigger);
        this.parentId = parentId;
        this.id = id;
    }
}
