package org.openforis.idm.db;

import static org.openforis.idm.db.IdmDatabaseSnapshotBuilder.COLUMN_NAME_QNAME;
import static org.openforis.idm.db.IdmDatabaseSnapshotBuilder.TABLE_NAME_QNAME;

import java.util.List;

import liquibase.database.structure.Column;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.transform2.ColumnProvider;
import org.openforis.idm.transform2.IllegalTransformationException;
import org.openforis.idm.transform2.NodeColumnProvider;
import org.openforis.idm.transform2.Transformation;
/**
 * 
 * @author G. Miceli
 *
 */
public class IdmDataTableBuilder extends AbstractIdmTableBuilder {

//	private EntityDefinition defn;
	private Transformation xform;

	public IdmDataTableBuilder(Transformation xform) {
		this.xform = xform;
	}

	@Override
	protected String getBaseName() {
		EntityDefinition defn = (EntityDefinition) xform.getNodeDefinition();
		String name = defn.getAnnotation(TABLE_NAME_QNAME);
		if ( name == null ) {
			name = defn.getName();
		}
		return name;
	}

	@Override
	protected void createColumns() {
		try {
			
			addIdColumn();
	
			// add columns for all data fields
			NodeColumnProvider provider = xform.getRootColumnProvider();
			List<org.openforis.idm.AbstractColumn.Column> cols = provider.getColumns();
			for (org.openforis.idm.AbstractColumn.Column col : cols) {
				NodeColumnProvider p = (NodeColumnProvider) col.getProvider();
//				NodeDefinition defn = p.getNodeDefinition();
//				addDataColumns((AttributeDefinition) child);
			}
	//		EntityDefinition defn = (EntityDefinition) xform.getNodeDefinition();
	//		List<NodeDefinition> childDefns = defn.getChildDefinitions();
	//		for (NodeDefinition child : childDefns) {
	//			if ( child instanceof AttributeDefinition ) {
	//				addDataColumns((AttributeDefinition) child);
	//			}
	//		}
		} catch (IllegalTransformationException e) {
			throw new RuntimeException(e);
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
