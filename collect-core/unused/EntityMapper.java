package org.openforis.collect.persistence.jooq;

import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * @author G. Miceli
 */
class EntityMapper extends NodeMapper {

	@Override
	Class<? extends NodeDefinition> getMappedClass() {
		return EntityDefinition.class;
	}

	@Override
	void setFields(Node<?> obj, InsertSetStep<?> insert) {
		// NOOP
	}

	@Override
	Entity addNode(NodeDefinition defn, Record r, Entity parent) {
		String name = defn.getName();
		return parent.addEntity(name);
	}
}
