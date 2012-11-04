package org.openforis.idm.db;

import static org.openforis.idm.db.IdmDatabaseSnapshotBuilder.TABLE_NAME_QNAME;
import liquibase.database.structure.Column;

import org.openforis.idm.metamodel.CodeList;

/**
 * @author G. Miceli
 */
public class IdmCodeListTableBuilder extends AbstractIdmTableBuilder {

	private CodeList list;

	public void setList(CodeList list) {
		this.list = list;
	}
	
	@Override
	protected String getBaseName() {
		String name = list.getAnnotation(TABLE_NAME_QNAME);
		if ( name == null ) {
			name = list.getName();
		}
		return name;
	}

	@Override
	protected void createColumns() {
		Column idCol = createIdColumn("id");
		addColumn(idCol);
	}
}
