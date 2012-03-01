package org.openforis.collect.persistence.jooq;

import static org.openforis.collect.persistence.jooq.tables.OfcData.DATA;

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
	void setFields(Node<?> node, InsertSetStep<?> insert) {
		Time value = ((TimeAttribute) node).getValue();
		if ( value != null ) {
			insert.set(DATA.NUMBER1, toNumeric(value.getHour()));
			insert.set(DATA.NUMBER2, toNumeric(value.getMinute()));
		}
	}

	@Override
	Node<?> addNode(NodeDefinition defn, Record r, Entity parent) {
		String name = defn.getName();
		Integer hour= r.getValueAsInteger(DATA.NUMBER1);
		Integer minute = r.getValueAsInteger(DATA.NUMBER2);
		Time time = new Time(hour, minute);
		return parent.addValue(name, time);	
	}
}
