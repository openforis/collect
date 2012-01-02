package org.openforis.collect.persistence.jooq;

import static org.openforis.collect.persistence.jooq.tables.Data.DATA;

import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.openforis.idm.metamodel.SchemaObjectDefinition;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.ModelObject;

/**
 * @author G. Miceli
 */
public class CodeAttributeRowMapper implements ModelObjectTypeMapper {

	@Override
	public Class<?> getMappedClass() {
		return CodeAttribute.class;
	}

	@Override
	public void setInsertFields(ModelObject<?> node, InsertSetStep<?> insert) {
		Code<?> value = ((CodeAttribute<?>) node).getValue();
		insert.set(DATA.TEXT1, String.valueOf(value.getCode()));
		insert.set(DATA.TEXT2, value.getQualifier());
	}

	@Override
	public Entity addObject(SchemaObjectDefinition defn, Record r, Entity parent) {
		// TODO
		return null;
	}
}
