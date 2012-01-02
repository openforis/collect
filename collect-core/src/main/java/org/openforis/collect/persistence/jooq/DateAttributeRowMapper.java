package org.openforis.collect.persistence.jooq;

import static org.openforis.collect.persistence.jooq.tables.Data.DATA;

import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.openforis.idm.metamodel.SchemaObjectDefinition;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.DateAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.ModelObject;

/**
 * @author G. Miceli
 */
public class DateAttributeRowMapper implements ModelObjectTypeMapper {

	@Override
	public Class<?> getMappedClass() {
		return DateAttribute.class;
	}

	@Override
	public void setInsertFields(ModelObject<?> node, InsertSetStep<?> insert) {
		Date value = ((DateAttribute) node).getValue();
		if ( value != null ) {
			insert.set(DATA.NUMBER1, value.getYear() == null ? null : value.getYear().doubleValue());
			insert.set(DATA.NUMBER2, value.getMonth() == null ? null : value.getMonth().doubleValue());
			insert.set(DATA.NUMBER3, value.getDay() == null ? null : value.getDay().doubleValue());
		}
	}

	@Override
	public Entity addObject(SchemaObjectDefinition defn, Record r, Entity parent) {
		// TODO
		return null;
	}
}
