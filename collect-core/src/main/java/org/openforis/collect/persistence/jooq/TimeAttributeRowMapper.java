package org.openforis.collect.persistence.jooq;

import static org.openforis.collect.persistence.jooq.tables.Data.DATA;

import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.openforis.idm.metamodel.SchemaObjectDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.ModelObject;
import org.openforis.idm.model.Time;
import org.openforis.idm.model.TimeAttribute;

/**
 * @author G. Miceli
 */
public class TimeAttributeRowMapper implements ModelObjectTypeMapper {

	@Override
	public Class<?> getMappedClass() {
		return TimeAttribute.class;
	}

	@Override
	public void setInsertFields(ModelObject<?> node, InsertSetStep<?> insert) {
		Time value = ((TimeAttribute) node).getValue();
		if ( value != null ) {
			insert.set(DATA.NUMBER1, value.getHour() == null ? null : value.getHour().doubleValue());
			insert.set(DATA.NUMBER2, value.getMinute() == null ? null : value.getMinute().doubleValue());
		}
	}

	@Override
	public Entity addObject(SchemaObjectDefinition defn, Record r, Entity parent) {
		// TODO
		return null;
	}
}
