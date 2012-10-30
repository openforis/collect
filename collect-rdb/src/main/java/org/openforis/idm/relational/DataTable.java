package org.openforis.idm.relational;

import javax.xml.namespace.QName;

import org.openforis.idm.metamodel.NodeDefinition;

/**
 * 
 * @author G. Miceli
 *
 */
class DataTable extends Table {

	private static final String RDB_NAMESPACE = "http://www.openforis.org/collect/3.0/rdb";
	private static final QName TABLE_NAME_QNAME = new QName(RDB_NAMESPACE, "table");
	private static final QName COLUMN_NAME_QNAME = new QName(RDB_NAMESPACE, "column");
	
	private NodeDefinition definition;
	private DataTable parentTable;
	
	DataTable(NodeDefinition defn, DataTable parentTable) {
		super(getTableName(defn));
		this.definition = defn;
		this.parentTable = parentTable;
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
}
