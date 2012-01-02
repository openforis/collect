package org.openforis.collect.persistence.jooq;

import static org.openforis.collect.persistence.jooq.tables.Data.DATA;

import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.openforis.idm.metamodel.SchemaObjectDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.ModelObject;
import org.openforis.idm.model.Time;
import org.openforis.idm.model.TimeAttribute;

/**
 * @author G. Miceli
 */
class TimeAttributeMapper extends ModelObjectMapper {

	@Override
	Class<? extends SchemaObjectDefinition> getMappedClass() {
		return TimeAttributeDefinition.class;
	}

	@Override
	void setInsertFields(ModelObject<?> node, InsertSetStep<?> insert) {
		Time value = ((TimeAttribute) node).getValue();
		if ( value != null ) {
			insert.set(DATA.NUMBER1, value.getHour() == null ? null : value.getHour().doubleValue());
			insert.set(DATA.NUMBER2, value.getMinute() == null ? null : value.getMinute().doubleValue());
		}
	}

	@Override
	ModelObject<?> addObject(SchemaObjectDefinition defn, Record r, Entity parent) {
		String name = defn.getName();
		Integer hour= r.getValueAsInteger(DATA.NUMBER1);
		Integer minute = r.getValueAsInteger(DATA.NUMBER2);
		Time time = new Time(hour, minute);
		return parent.addValue(name, time);	
	}
}
