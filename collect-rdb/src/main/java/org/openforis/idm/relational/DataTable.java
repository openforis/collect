package org.openforis.idm.relational;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.metamodel.NodeDefinition;

/**
 * 
 * @author G. Miceli
 *
 */
class DataTable extends Table {

	private static final String CODE_QUALIFER_COLUMN_SUFFIX = "_other";
	private static final String RDB_NAMESPACE = "http://www.openforis.org/collect/3.0/rdb";
	private static final QName TABLE_NAME_QNAME = new QName(RDB_NAMESPACE, "table");
	private static final QName COLUMN_NAME_QNAME = new QName(RDB_NAMESPACE, "column");
	
	private NodeDefinition definition;
	private LinkedHashMap<String, Column> columnsByPath; 
	
	DataTable(NodeDefinition defn) throws SchemaGenerationException {
		super(getTableName(defn));
		this.definition = defn;
		this.columnsByPath = new LinkedHashMap<String, Column>();		
		addDataColumns(defn);		
	}

	public NodeDefinition getNodeDefinition() {
		return definition;
	}
	
	private void addDataColumns(NodeDefinition defn) throws SchemaGenerationException {
		if ( defn instanceof EntityDefinition ) {
			addDataColumns((EntityDefinition) defn);
		} else if ( defn instanceof AttributeDefinition ) {
			addDataColumns((AttributeDefinition) defn);
		} else if ( defn instanceof FieldDefinition ) {
			addDataColumns((FieldDefinition<?>) defn);
		} else {
			throw new UnsupportedOperationException("Unknown node "+defn.getClass());
		}
	}
	
	private void addDataColumns(EntityDefinition defn) throws SchemaGenerationException {
		List<NodeDefinition> childDefinitions = defn.getChildDefinitions();
		for (NodeDefinition child : childDefinitions) {
			addDataColumns(child);
		}
	}
	
	private void addDataColumns(AttributeDefinition defn) throws SchemaGenerationException {
		List<FieldDefinition<?>> fieldDefinitions = defn.getFieldDefinitions();
		if ( defn instanceof CodeAttributeDefinition ) {
			addDataColumns((CodeAttributeDefinition) defn);
		} else if ( fieldDefinitions.size() == 1 ) {
			String name = getColumnName(defn);
			addDataColumn(name, fieldDefinitions.get(0));
		} else {
			for (FieldDefinition<?> field : fieldDefinitions) {
				addDataColumns(field);
			}
		}
	}
	
	private void addDataColumns(CodeAttributeDefinition defn) throws SchemaGenerationException {
		List<FieldDefinition<?>> fieldDefinitions = defn.getFieldDefinitions();
		String name = getColumnName(defn);
		addDataColumn(name, fieldDefinitions.get(0));
		CodeList list = defn.getList();
		if ( list.isQualifiable() ) {
			addDataColumn(name+CODE_QUALIFER_COLUMN_SUFFIX, fieldDefinitions.get(1));			
		}
	}
	
	private void addDataColumn(String name, FieldDefinition<?> field) throws SchemaGenerationException {
		Class<?> valueType = field.getValueType();
		addDataColumn(name, valueType, field);
	}

	private void addDataColumns(FieldDefinition<?> defn) throws SchemaGenerationException {
		String name = getColumnName(defn);
		Class<?> valueType = defn.getValueType();
		addDataColumn(name, valueType, defn);
	}

	protected void addDataColumn(String name, Class<?> valueType, NodeDefinition defn) throws SchemaGenerationException {
		Column col = new Column(name, valueType, true);
		addColumn(col);
		columnsByPath.put(defn.getPath(), col);
	}

	private static String getTableName(NodeDefinition defn) {
		String name = defn.getAnnotation(TABLE_NAME_QNAME);
		if ( name == null ) {
			StringBuilder sb = new StringBuilder();
			buildTableName(defn, sb);
			name = sb.toString();
		}
		return name;
	}

	private static void buildTableName(NodeDefinition defn, StringBuilder sb) {
		NodeDefinition parent = defn.getParentDefinition();
		if ( parent != null && !parent.isMultiple() ) {
			buildTableName(parent, sb);
			sb.append('_');
		}
		sb.append(defn.getName());
	}
	
	private String getColumnName(NodeDefinition defn) {
		String name = defn.getAnnotation(COLUMN_NAME_QNAME);
		if ( name == null ) {
			if ( defn == definition ) {
				// e.g. multiple number attribute
				name = defn.getName();
//				name = SINGLE_VALUE_TABLE_COLUMN;
			} else {
				StringBuilder sb = new StringBuilder();
				buildColumnName(defn, sb);
				name = sb.toString();
			}
		}
		return name;
	}

	private void buildColumnName(NodeDefinition defn, StringBuilder sb) {
		NodeDefinition parent = defn.getParentDefinition();
		if ( parent != definition ) {
			buildColumnName(parent, sb);
			sb.append('_');
		}
		sb.append(defn.getName());
	}

	public void printDebug(PrintStream out) {
		out.printf("%-35s%s\n", getName()+":", definition.getPath());
		Set<Entry<String, Column>> es = columnsByPath.entrySet();
		for (Entry<String, Column> e : es) {
			Column col = e.getValue();
			String name = col.getName();
			Class<?> type = col.getType();
			String path = e.getKey();
			out.printf("\t%-45s%-20s%s\n", name, type.getSimpleName(), path);
		}
		out.flush();
	}
}