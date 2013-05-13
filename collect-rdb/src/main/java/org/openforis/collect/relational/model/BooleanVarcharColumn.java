/**
 * 
 */
package org.openforis.collect.relational.model;

import java.sql.Types;

import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.path.Path;

/**
 * @author G. Miceli
 *
 */
public class BooleanVarcharColumn extends DataColumn {
	
	private String trueValue;
	private String falseValue;

	BooleanVarcharColumn(String name, FieldDefinition<Boolean> defn,
			Path relPath, String trueValue, String falseValue, String defaultValue) {
		super(name, Types.VARCHAR, "varchar", defn, relPath);

		int length = calculateMaxLength(trueValue, falseValue, defaultValue);
		setDefaultValue(defaultValue);
		setNullable(defaultValue == null);
		setLength(length);
		
		this.trueValue = trueValue;
		this.falseValue = falseValue;
	}

	private int calculateMaxLength(String trueValue, String falseValue, String defaultValue) {
		int length = Math.max(trueValue.length(), falseValue.length());
		if ( defaultValue != null ) {
			length = Math.max(length, defaultValue.length()); 
		}
		return length;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Object convert(Node<?> valNode) {
		if ( valNode == null ) {
			return null;
		}
		Field<Boolean> fld = (Field<Boolean>) valNode;
		Boolean val = fld.getValue();
		if ( val == null ) {
			return null;
		} else if ( val ) {
			return trueValue;
		} else {
			return falseValue;
		}
	}
}
