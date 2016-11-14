package org.openforis.collect.relational.model;

import org.openforis.collect.relational.sql.RDBJdbcType;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Node;
import org.openforis.idm.path.Path;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class DataColumn extends AbstractColumn<Node<?>> {
	
	private Object defaultValue;
	private NodeDefinition nodeDefinition;
	private Path relativePath;

	protected static Integer getFieldLength(FieldDefinition<?> defn) {
		Class<?> type = defn.getValueType();
		return getFieldLength(type);
	}

	protected static Integer getFieldLength(Class<?> type) {
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
	
	DataColumn(String name, RDBJdbcType type, NodeDefinition defn, Path relPath, Integer length, boolean nullable) {
		this(name, type, defn, relPath, length, nullable, null);
	}

	DataColumn(String name, RDBJdbcType type, NodeDefinition defn, Path relPath, Integer length, boolean nullable, Object defaultValue) {
		super(name, type, length, nullable);
		this.nodeDefinition = defn;
		this.relativePath = relPath;
		this.defaultValue = defaultValue;
	}

	public NodeDefinition getNodeDefinition() {
		return nodeDefinition;
	}
	
	public Path getRelativePath() {
		return relativePath;
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	/**
	 * Returns the {@link AttributeDefinition} associated to the column.
	 * 
	 * @param column
	 * @return
	 */
	public AttributeDefinition getAttributeDefinition() {
		if ( nodeDefinition == null ) {
			return null;
		}
		AttributeDefinition attributeDefn;
		if ( nodeDefinition instanceof AttributeDefinition ) {
			attributeDefn = (AttributeDefinition) nodeDefinition;
		} else if ( nodeDefinition instanceof FieldDefinition ) {
			attributeDefn = (AttributeDefinition) nodeDefinition.getParentDefinition();
		} else {
			throw new IllegalStateException(
					"Invalid node definition, expected AttributeDefinition or FieldDefinition, found: "
							+ nodeDefinition.getClass().getName());
		}
		return attributeDefn;
	}

}
