/**
 * 
 */
package org.openforis.collect.relational.model;

import java.sql.Types;

import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.model.Node;
import org.openforis.idm.path.Path;

/**
 * @author S. Ricci
 *
 */
public class AncestorKeyColumn extends DataColumn {
	
	AncestorKeyColumn(String name, FieldDefinition<?> defn, Path relPath) {
		super(name, getJdbcType(defn), getTypeName(defn), defn, 
				relPath, getFieldLength(defn), true);
	}
	
	protected static int getJdbcType(FieldDefinition<?> defn) {
		Class<?> type = defn.getValueType();
		if ( type == Integer.class ) {
			return Types.INTEGER;
		} else if ( type == Double.class ) {
			return Types.FLOAT;
		} else if ( type == String.class ) {
			return Types.VARCHAR;
		} else {
			throw new UnsupportedOperationException("Unknown field type "+type);				
		}
	}

	protected static String getTypeName(FieldDefinition<?> defn) {
		Class<?> type = defn.getValueType();
		if ( type == Integer.class ) {
			return "integer";
		} else if ( type == Double.class ) {
			return "float";
		} else if ( type == String.class ) {
			return "varchar";
		} else {
			throw new UnsupportedOperationException("Unknown field type "+type);				
		}
	}
	
	protected static Integer getFieldLength(FieldDefinition<?> defn) {
		Class<?> type = defn.getValueType();
		if ( type == Integer.class ) {
			return null;
		} else if ( type == Double.class ) {
			return 24;
		} else if ( type == String.class ) {
			return 255;
		} else {
			throw new UnsupportedOperationException("Unknown field type "+type);				
		}
	}
	
	@Override
	public Object extractValue(Node<?> context) {
		return super.extractValue(context);
	}

}
