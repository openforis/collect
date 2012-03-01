package org.openforis.collect.persistence.jooq;

import static org.openforis.collect.persistence.jooq.tables.OfcData.DATA;

import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * @author G. Miceli
 */
class CodeAttributeMapper extends NodeMapper {

	@Override
	Class<? extends NodeDefinition> getMappedClass() {
		return CodeAttributeDefinition.class;
	}

	@Override
	void setFields(Node<?> node, InsertSetStep<?> insert) {
		Code value = ((CodeAttribute) node).getValue();
		if ( value != null ) {
			insert.set(DATA.TEXT1, value.getCode());
			insert.set(DATA.TEXT2, value.getQualifier());
		}
	}

	@Override
	CodeAttribute addNode(NodeDefinition defn, Record r, Entity parent) {
		String name = defn.getName();
		String qualifier = r.getValueAsString(DATA.TEXT2);

		CodeAttributeDefinition codeAttrDefn = (CodeAttributeDefinition) defn;
		CodeList list = codeAttrDefn.getList();
		String code = r.getValueAsString(DATA.TEXT1);
		Code value = new Code(code, qualifier);
		return parent.addValue(name, value);
	}
}
