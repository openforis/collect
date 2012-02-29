/**
 * 
 */
package org.openforis.collect.persistence.jooq;

import static org.openforis.collect.persistence.jooq.tables.OfcData.DATA;

import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * @author M. Togna
 * 
 */
public class CoordinateAttributeMapper extends NodeMapper {

	@Override
	Class<? extends NodeDefinition> getMappedClass() {
		return CoordinateAttributeDefinition.class;
	}

	@Override
	void setFields(Node<?> node, InsertSetStep<?> insert) {
		CoordinateAttribute c = (CoordinateAttribute) node;
		Coordinate value = c.getValue();
		if (value != null) {
			insert.set(DATA.NUMBER1, toNumeric(value.getX()));
			insert.set(DATA.NUMBER2, toNumeric(value.getY()));
			//insert.set(DATA.NUMBER3, toNumeric(value.getZ()));
			insert.set(DATA.TEXT1, value.getSrsId());
		}
	}

	@Override
	Node<?> addNode(NodeDefinition defn, Record r, Entity parent) {
		Long x = r.getValueAsLong(DATA.NUMBER1);
		Long y = r.getValueAsLong(DATA.NUMBER2);
//		Long z = r.getValueAsLong(DATA.NUMBER3);
		String srsId = r.getValueAsString(DATA.TEXT1);
		Coordinate c = new Coordinate(x, y, srsId);
		return parent.addValue(defn.getName(), c);
	}

}
