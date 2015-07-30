package org.openforis.collect.relational.jooq;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.jooq.CreateTableAsStep;
import org.jooq.CreateTableColumnStep;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.openforis.collect.persistence.jooq.CollectDSLContext;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.RelationalSchemaCreator;
import org.openforis.collect.relational.model.Column;
import org.openforis.collect.relational.model.PrimaryKeyColumn;
import org.openforis.collect.relational.model.PrimaryKeyConstraint;
import org.openforis.collect.relational.model.ReferentialConstraint;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.Table;

public class JooqRelationalSchemaCreator implements RelationalSchemaCreator {

	@Override
	public void createRelationalSchema(RelationalSchema schema, Connection conn)
			throws CollectRdbException {
		CollectDSLContext dsl = new CollectDSLContext(conn);
		
		for (Table<?> table : schema.getTables()) {
			org.jooq.Table<Record> jooqTable = createJooqTable(schema, table, ! dsl.isSchemaLess());
			CreateTableAsStep<Record> createTableStep = dsl.createTable(jooqTable);
			Query createTableFinalQuery = (Query) createTableStep;
			for (Column<?> column : table.getColumns()) {
				DataType<?> dataType = DefaultDataType.getDataType(dsl.getDialect(), column.getTypeName());
				Integer length = column.getLength();
				if (length != null) {
					dataType.length(length);
				}
				CreateTableColumnStep columnStep = createTableStep.column(column.getName(), dataType);
				
				if (column instanceof PrimaryKeyColumn) {
					//TODO
//					columnStep.
				}
				createTableFinalQuery = columnStep;
			}
			createTableFinalQuery.execute();

			PrimaryKeyConstraint pkConstraint = table.getPrimaryKeyConstraint();
			String pkColumnName = pkConstraint.getPrimaryKeyColumn().getName();
			dsl.alterTable(jooqTable).add(
					DSL.constraint(table.getName() + "_pk").primaryKey(pkColumnName)
				).execute();
		}
		
		if(dsl.isForeignKeySupported()){
			createForeignKeys(schema, dsl);
		}
	}

	private void createForeignKeys(RelationalSchema schema, CollectDSLContext dsl) {
		for (org.openforis.collect.relational.model.Table<?> table : schema.getTables()) {
			List<ReferentialConstraint> fks = table.getReferentialContraints();
			for (ReferentialConstraint fk : fks) {
				Field<?>[] fields = toJooqFields(fk.getColumns());
				String referencedTableName = fk.getReferencedKey().getTable().getName();
				List<Column<?>> referencedColumns = fk.getReferencedKey().getColumns();
				String[] referencedColumnNames = extractNames(referencedColumns);
				dsl.alterTable(createJooqTable(schema, table, ! dsl.isSchemaLess()))
					.add(DSL.constraint(fk.getName())
							.foreignKey(fields)
							.references(referencedTableName, referencedColumnNames))
					.execute();
			}
		}
	}

	private Field<?>[] toJooqFields(List<Column<?>> fkColumns) {
		List<Field<?>> fields = new ArrayList<Field<?>>(fkColumns.size());
		for (Column<?> fkColumn : fkColumns) {
			fields.add(DSL.field(fkColumn.getName()));
		}
		return fields.toArray(new Field<?>[fields.size()]);
	}

	private String[] extractNames(List<Column<?>> columns) {
		List<String> names = new ArrayList<String>(columns.size());
		for (Column<?> column : columns) {
			names.add(column.getName());
		}
		return names.toArray(new String[columns.size()]);
	}
	
	private org.jooq.Table<Record> createJooqTable(RelationalSchema schema,
			org.openforis.collect.relational.model.Table<?> table, boolean renderSchema) {
		if (renderSchema) {
			return DSL.tableByName(schema.getName(), table.getName());
		} else {
			return DSL.tableByName(table.getName());
		}
	}

//	synchronized
//	public DatabaseSnapshot createSnapshot(RelationalSchema relationalSchema, boolean dbSupportsFKs) throws DatabaseException {
//		this.schema = relationalSchema;
//		UnsupportedDatabase db = new UnsupportedDatabase();
//		snapshot = new DatabaseSnapshot(db, null);
//
//		createTables();
//		
//		if( dbSupportsFKs ){
//			createForeignKeys();
//		}
//		
//		// TODO
//		
//		return snapshot;
//	}
//
//	protected void createForeignKeys() {
//		for (org.openforis.collect.relational.model.Table<?> itable : schema.getTables()) {
//			Table ltable = snapshot.getTable(itable.getName());
//			List<ReferentialConstraint> ifks = itable.getReferentialContraints();
//			for (ReferentialConstraint ifk : ifks) {
//				ForeignKey lfk = new ForeignKey();
//				lfk.setName(ifk.getName());
//				//set base table columns
//				lfk.setForeignKeyTable(ltable);
//				for (org.openforis.collect.relational.model.Column<?> ifcCol : ifk.getColumns()) {
//					lfk.addForeignKeyColumn(ifcCol.getName());
//				}
//				//set referenced key columns
//				UniquenessConstraint iReferencedKey = ifk.getReferencedKey();
//				org.openforis.collect.relational.model.Table<?> iReferencedTable = iReferencedKey.getTable();
//				Table lReferencedTable = snapshot.getTable(iReferencedTable.getName());
//				lfk.setPrimaryKeyTable(lReferencedTable);
//				for (org.openforis.collect.relational.model.Column<?> refCol : iReferencedKey.getColumns()) {
//					lfk.addPrimaryKeyColumn(refCol.getName());
//				}
//				//Add fk
//				snapshot.getForeignKeys().add(lfk);
//			}
//		}
//	}
}
