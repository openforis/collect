package org.openforis.collect.persistence.jooq;

import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.SchemaObjectDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.ModelObject;

/**
 * @author G. Miceli
 */
class EntityRowMapper extends ModelObjectMapper {

	@Override
	Class<? extends SchemaObjectDefinition> getMappedClass() {
		return EntityDefinition.class;
	}

	@Override
	void setInsertFields(ModelObject<?> obj, InsertSetStep<?> insert) {
		// NOOP
	}

	@Override
	Entity addObject(SchemaObjectDefinition defn, Record r, Entity parent) {
		String name = defn.getName();
		return parent.addEntity(name);
	}
}
