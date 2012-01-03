package org.openforis.collect.persistence.jooq;

import static org.openforis.collect.persistence.jooq.tables.Data.DATA;

import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.AlphanumericCode;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NumericCode;

/**
 * @author G. Miceli
 */
class CodeAttributeMapper extends NodeMapper {

	@Override
	Class<? extends NodeDefinition> getMappedClass() {
		return CodeAttributeDefinition.class;
	}

	@Override
	void setInsertFields(Node<?> node, InsertSetStep<?> insert) {
		Code<?> value = ((CodeAttribute<?>) node).getValue();
		if ( value instanceof NumericCode){
			Integer code = ((NumericCode)value).getCode();
			insert.set(DATA.NUMBER1, code == null ? null : code.doubleValue());
			insert.set(DATA.TEXT2, value.getQualifier());
		} else {
			insert.set(DATA.TEXT1, ((AlphanumericCode)value).getCode());
			insert.set(DATA.TEXT2, value.getQualifier());
		} 
	}

	@Override
	CodeAttribute<?> addObject(NodeDefinition defn, Record r, Entity parent) {
		String name = defn.getName();
		String qualifier = r.getValueAsString(DATA.TEXT2);

		CodeAttributeDefinition codeAttrDefn = (CodeAttributeDefinition) defn;
		CodeList list = codeAttrDefn.getList();
		if ( list.isNumeric() ) {
			Integer code = r.getValueAsInteger(DATA.NUMBER1);
			NumericCode value = new NumericCode(code, qualifier);
			return parent.addValue(name, value);
		} else {
			String code = r.getValueAsString(DATA.TEXT1);
			AlphanumericCode value = new AlphanumericCode(code, qualifier);
			return parent.addValue(name, value);
		}
		
	}
}
