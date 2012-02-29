/**
 * 
 */
package org.openforis.collect.persistence.jooq;

import static org.openforis.collect.persistence.jooq.tables.OfcData.DATA;

import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.BooleanAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * @author M. Togna
 * 
 */
class BooleanAttributeMapper extends NodeMapper {

	@Override
	Class<? extends NodeDefinition> getMappedClass() {
		return BooleanAttributeDefinition.class;
	}

	@Override
	void setFields(Node<?> node, InsertSetStep<?> insert) {
		Boolean value = ((BooleanAttribute) node).getValue();
		insert.set(DATA.NUMBER1, toNumeric(value));
	}

	@Override
	Node<?> addNode(NodeDefinition defn, Record r, Entity parent) {
		Double v = r.getValueAsDouble(DATA.NUMBER1);
		Boolean b = null;
		if (v != null) {
			if (v.equals(Double.valueOf(1))) {
				b = Boolean.TRUE;
			} else if (v.equals(Double.valueOf(0))) {
				b = Boolean.FALSE;
			}
		}
		return parent.addValue(defn.getName(), b);
	}
}
