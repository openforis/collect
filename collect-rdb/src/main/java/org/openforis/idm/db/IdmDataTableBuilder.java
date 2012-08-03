package org.openforis.idm.db;

import static org.openforis.idm.db.IdmDatabaseSnapshotBuilder.COLUMN_NAME_QNAME;
import static org.openforis.idm.db.IdmDatabaseSnapshotBuilder.TABLE_NAME_QNAME;

import java.sql.Types;
import java.util.List;

import liquibase.database.structure.Column;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
/**
 * 
 * @author G. Miceli
 *
 */
public class IdmDataTableBuilder extends AbstractIdmTableBuilder {

	private EntityDefinition defn;

	public IdmDataTableBuilder(EntityDefinition defn) {
		this.defn = defn;
	}

	@Override
	protected String getBaseName() {
		String name = defn.getAnnotation(TABLE_NAME_QNAME);
		if ( name == null ) {
			name = defn.getName();
		}
		return name;
	}

	@Override
	protected void createColumns() {
		
		addIdColumn();

		// add columns for all data fields
		List<NodeDefinition> childDefns = defn.getChildDefinitions();
		for (NodeDefinition child : childDefns) {
			if ( child instanceof AttributeDefinition ) {
				addDataColumns((AttributeDefinition) child);
			}
		}
	}

	private void addIdColumn() {
		Column idCol = createIdColumn("_id");
		addColumn(idCol);
	}

	private void addDataColumns(AttributeDefinition attr) {
		String name = getColumnBaseName(attr);
		addAttributeColumn(name, "varchar(255)");
	}

	private void addAttributeColumn(String name, String type) {
		Column col = new Column();
		col.setName(name);
		col.setNullable(true);
		col.setTypeName(type);
		addColumn(col);
	}

	private String getColumnBaseName(AttributeDefinition attr) {
		String name = attr.getAnnotation(COLUMN_NAME_QNAME);
		if ( name == null ) {
			name = attr.getName();
		}
		return name;
	}
}
