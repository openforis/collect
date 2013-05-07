package org.openforis.collect.relational.liquibase;

import java.util.List;

import liquibase.database.Database;
import liquibase.database.core.UnsupportedDatabase;
import liquibase.database.structure.Column;
import liquibase.database.structure.ForeignKey;
import liquibase.database.structure.PrimaryKey;
import liquibase.database.structure.Table;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;

import org.openforis.collect.relational.model.PrimaryKeyColumn;
import org.openforis.collect.relational.model.ReferentialConstraint;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.UniquenessConstraint;

/**
 * @author G. Miceli
 */
class LiquidbaseDatabaseSnapshotBuilder {

	private RelationalSchema schema;
	DatabaseSnapshot snapshot;
	
	public LiquidbaseDatabaseSnapshotBuilder() {
	}

	synchronized
	public DatabaseSnapshot createSnapshot(RelationalSchema relationalSchema) throws DatabaseException {
		this.schema = relationalSchema;
		UnsupportedDatabase db = new UnsupportedDatabase();
		snapshot = new DatabaseSnapshot(db, null);

		createCodeListTables();
		createDataTables();
		
		createForeignKeys();
		
		// TODO
		
		return snapshot;
	}

	private void createCodeListTables() {
		// TODO Auto-generated method stub
		
	}

	private void createDataTables() {
		// Create table
		for (org.openforis.collect.relational.model.Table<?> itable : schema.getTables()) {
			Table ltable = new Table(itable.getName());
			Database db = snapshot.getDatabase();
			ltable.setDatabase(db);
			ltable.setSchema(schema.getName());
			ltable.setRawSchemaName(schema.getName());
			// Create columns
			for (org.openforis.collect.relational.model.Column<?> icolumn : itable.getColumns()) {
				Column lcolumn = new Column();
				lcolumn.setTable(ltable);
				lcolumn.setName(icolumn.getName());
				lcolumn.setNullable(icolumn.isNullable());
				lcolumn.setDataType(icolumn.getType());
				lcolumn.setTypeName(icolumn.getTypeName());
				if ( icolumn.getLength() != null ) {
					lcolumn.setColumnSize(icolumn.getLength());
				}
				// Set PK
				if ( icolumn instanceof PrimaryKeyColumn ) {
					lcolumn.setPrimaryKey(true);
					lcolumn.setUnique(true);
					// Add PK constraint
					PrimaryKey lpk = new PrimaryKey();
					lpk.setTable(ltable);
					lpk.setName(itable.getPrimaryKeyConstraint().getName());
					lpk.getColumnNamesAsList().add(lcolumn.getName());
					snapshot.getPrimaryKeys().add(lpk);
				}
				// Add column
				ltable.getColumns().add(lcolumn);
			}
			// Add table
			snapshot.getTables().add(ltable);
		}
	}

	protected void createForeignKeys() {
		for (org.openforis.collect.relational.model.Table<?> itable : schema.getTables()) {
			Table ltable = snapshot.getTable(itable.getName());
			List<ReferentialConstraint> ifks = itable.getReferentialContraints();
			for (ReferentialConstraint ifk : ifks) {
				ForeignKey lfk = new ForeignKey();
				lfk.setName(ifk.getName());
				//set base table columns
				lfk.setForeignKeyTable(ltable);
				for (org.openforis.collect.relational.model.Column<?> ifcCol : ifk.getColumns()) {
					lfk.addForeignKeyColumn(ifcCol.getName());
				}
				//set referenced key columns
				UniquenessConstraint iReferencedKey = ifk.getReferencedKey();
				org.openforis.collect.relational.model.Table<?> iReferencedTable = iReferencedKey.getTable();
				Table lReferencedTable = snapshot.getTable(iReferencedTable.getName());
				lfk.setPrimaryKeyTable(lReferencedTable);
				for (org.openforis.collect.relational.model.Column<?> refCol : iReferencedKey.getColumns()) {
					lfk.addPrimaryKeyColumn(refCol.getName());
				}
				//Add fk
				snapshot.getForeignKeys().add(lfk);
			}
		}
	}
}
