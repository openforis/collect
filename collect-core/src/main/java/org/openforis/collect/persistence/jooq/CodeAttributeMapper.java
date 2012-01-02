package org.openforis.collect.persistence.jooq;

import static org.openforis.collect.persistence.jooq.tables.Data.DATA;

import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.SchemaObjectDefinition;
import org.openforis.idm.model.AlphanumericCode;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.ModelObject;

/**
 * @author G. Miceli
 */
class CodeAttributeMapper extends ModelObjectMapper {

	@Override
	Class<? extends SchemaObjectDefinition> getMappedClass() {
		return CodeAttributeDefinition.class;
	}

	@Override
	void setInsertFields(ModelObject<?> node, InsertSetStep<?> insert) {
		Code<?> value = ((CodeAttribute<?>) node).getValue();
		insert.set(DATA.TEXT1, String.valueOf(value.getCode()));
		insert.set(DATA.TEXT2, value.getQualifier());
	}

	@Override
	CodeAttribute<?> addObject(SchemaObjectDefinition defn, Record r, Entity parent) {
		String name = defn.getName();
		String code = r.getValueAsString(DATA.TEXT1);
		String qualifier = r.getValueAsString(DATA.TEXT2);
		AlphanumericCode value = new AlphanumericCode(code, qualifier);
		// TODO How can we know whether the code is alphanum or num???  We need to know what coding scheme the code is in!!!
		return parent.addValue(name, value);
		
	}
}
