package org.openforis.collect.persistence.jooq;

import static org.openforis.collect.persistence.jooq.tables.OfcData.DATA;

import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NumberAttribute;

/**
 * @author G. Miceli
 */
class NumberAttributeMapper extends NodeMapper {

	@Override
	Class<? extends NodeDefinition> getMappedClass() {
		return NumberAttributeDefinition.class;
	}

	@Override
	void setFields(Node<?> node, InsertSetStep<?> insert) {
		Number value = ((NumberAttribute<?>) node).getValue();
		insert.set(DATA.NUMBER1, toNumeric(value));
	}

	@Override
	NumberAttribute<?> addNode(NodeDefinition defn, Record r, Entity parent) {
		String name = defn.getName();
		if ( ((NumberAttributeDefinition) defn).isInteger() ) {
			Integer value = r.getValueAsInteger(DATA.NUMBER1);
			return parent.addValue(name, value);
		} else if ( ((NumberAttributeDefinition) defn).isReal() ) {
			Double value = r.getValueAsDouble(DATA.NUMBER1);
			return parent.addValue(name, value);		
		} else {
			throw new RuntimeException("Unimplemented numeric type "+((NumberAttributeDefinition)defn).getType());
		}
	}
}
