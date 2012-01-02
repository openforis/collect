package org.openforis.collect.persistence.jooq;

import static org.openforis.collect.persistence.jooq.tables.Data.DATA;

import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.SchemaObjectDefinition;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.DateAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.ModelObject;

/**
 * @author G. Miceli
 */
class DateAttributeMapper extends ModelObjectMapper {

	@Override
	Class<? extends SchemaObjectDefinition> getMappedClass() {
		return DateAttributeDefinition.class;
	}

	@Override
	void setInsertFields(ModelObject<?> node, InsertSetStep<?> insert) {
		Date value = ((DateAttribute) node).getValue();
		if ( value != null ) {
			insert.set(DATA.NUMBER1, value.getYear() == null ? null : value.getYear().doubleValue());
			insert.set(DATA.NUMBER2, value.getMonth() == null ? null : value.getMonth().doubleValue());
			insert.set(DATA.NUMBER3, value.getDay() == null ? null : value.getDay().doubleValue());
		}
	}

	@Override
	ModelObject<?> addObject(SchemaObjectDefinition defn, Record r, Entity parent) {
		Integer year = r.getValueAsInteger(DATA.NUMBER1);
		Integer month = r.getValueAsInteger(DATA.NUMBER2);
		Integer day = r.getValueAsInteger(DATA.NUMBER3);
		Date date = new Date(year, month, day);
		return parent.addValue(defn.getName(), date);
	}
}
