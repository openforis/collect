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
public class EntityRowMapper implements ModelObjectTypeMapper {

	@Override
	public Class<?> getMappedClass() {
		return EntityDefinition.class;
	}

	@Override
	public void setInsertFields(ModelObject<?> obj, InsertSetStep<?> insert) {
		// NOOP
	}

	@Override
	public Entity addObject(SchemaObjectDefinition defn, Record r, Entity parent) {
		String name = defn.getName();
		Entity entity = parent.addEntity(name);
		return entity;
	}
}
