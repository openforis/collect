package org.openforis.collect.persistence.jooq;

import static org.openforis.collect.persistence.jooq.tables.Data.DATA;

import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.openforis.idm.metamodel.SchemaObjectDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.ModelObject;
import org.openforis.idm.model.NumberAttribute;

/**
 * @author G. Miceli
 */
public class NumberAttributeRowMapper implements ModelObjectTypeMapper {

	@Override
	public Class<?> getMappedClass() {
		return NumberAttribute.class;
	}

	@Override
	public void setInsertFields(ModelObject<?> node, InsertSetStep<?> insert) {
		Number value = ((NumberAttribute<?>) node).getValue();
		insert.set(DATA.NUMBER1, value == null ? null : value.doubleValue());
	}

	@Override
	public Entity addObject(SchemaObjectDefinition defn, Record r, Entity parent) {
		// TODO
		return null;
	}
}
