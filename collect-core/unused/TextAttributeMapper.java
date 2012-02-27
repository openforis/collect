/**
 * 
 */
package org.openforis.collect.persistence.jooq;

import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.openforis.collect.persistence.jooq.tables.Data;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.TextAttribute;

/**
 * @author M. Togna
 * 
 */
public class TextAttributeMapper extends NodeMapper {

	@Override
	Class<? extends NodeDefinition> getMappedClass() {
		return TextAttributeDefinition.class;
	}

	@Override
	void setFields(Node<?> node, InsertSetStep<?> insert) {
		TextAttribute t = (TextAttribute) node;
		String value = t.getValue();
		insert.set(Data.DATA.TEXT1, value);
	}

	@Override
	Node<?> addNode(NodeDefinition defn, Record r, Entity parent) {
		String value = r.getValueAsString(Data.DATA.TEXT1);
		return parent.addValue(defn.getName(), value);
	}

}
