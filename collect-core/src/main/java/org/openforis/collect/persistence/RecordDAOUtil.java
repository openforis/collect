package org.openforis.collect.persistence;

import org.jooq.TableField;
import org.openforis.collect.persistence.jooq.tables.records.RecordRecord;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class RecordDAOUtil {
	
	public static TableField<RecordRecord, ?> getKeyField( org.openforis.collect.persistence.jooq.tables.Record r, AttributeDefinition def, int position) {
		if(def instanceof CodeAttributeDefinition || def instanceof TextAttributeDefinition) {
			switch(position) {
				case 1:
					return r.KEY_TEXT1;
				case 2:
					return r.KEY_TEXT2;
				case 3:
					return r.KEY_TEXT3;
				default:
					throw new RuntimeException("Exceeded maximum number of supported keys for this root entity");
			}
		} else if(def instanceof NumberAttributeDefinition) {
			switch(position) {
				case 1:
					return r.KEY_NUMBER1;
				case 2:
					return r.KEY_NUMBER2;
				case 3:
					return r.KEY_NUMBER3;
				default:
					throw new RuntimeException("Exceeded maximum number of supported keys for this root entity");
			}
		} else {
			throw new RuntimeException("AttributeDefinition type not supported: " + def.getClass().getSimpleName());
		}
	}
	
	public static TableField<RecordRecord, Integer> getCountField( org.openforis.collect.persistence.jooq.tables.Record r, int position) {
		switch(position) {
			case 1:
				return r.COUNT1;
			case 2:
				return r.COUNT2;
			case 3:
				return r.COUNT3;
			case 4:
				return r.COUNT4;
			case 5:
				return r.COUNT5;
			default:
				throw new RuntimeException("Exceeded maximum number of countable entities");
		}
	}
}
