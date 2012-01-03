package org.openforis.collect.persistence.jooq;

import static org.openforis.collect.persistence.jooq.tables.Data.DATA;

import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Time;
import org.openforis.idm.model.TimeAttribute;

/**
 * @author G. Miceli
 */
class TimeAttributeMapper extends NodeMapper {

	@Override
	Class<? extends NodeDefinition> getMappedClass() {
		return TimeAttributeDefinition.class;
	}

	@Override
	void setInsertFields(Node<?> node, InsertSetStep<?> insert) {
		Time value = ((TimeAttribute) node).getValue();
		if ( value != null ) {
			insert.set(DATA.NUMBER1, value.getHour() == null ? null : value.getHour().doubleValue());
			insert.set(DATA.NUMBER2, value.getMinute() == null ? null : value.getMinute().doubleValue());
		}
	}

	@Override
	Node<?> addObject(NodeDefinition defn, Record r, Entity parent) {
		String name = defn.getName();
		Integer hour= r.getValueAsInteger(DATA.NUMBER1);
		Integer minute = r.getValueAsInteger(DATA.NUMBER2);
		Time time = new Time(hour, minute);
		return parent.addValue(name, time);	
	}
}
