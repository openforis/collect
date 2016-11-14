package org.openforis.collect.relational.jooq;

import static org.jooq.impl.DSL.constraint;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.jooq.CollectCreateIndexStep;
import org.jooq.Condition;
import org.jooq.CreateTableAsStep;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.TableLike;
import org.jooq.impl.DSL;
import org.openforis.collect.persistence.jooq.CollectDSLContext;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.RelationalSchemaCreator;
import org.openforis.collect.relational.model.CodeListCodeColumn;
import org.openforis.collect.relational.model.CodeTable;
import org.openforis.collect.relational.model.Column;
import org.openforis.collect.relational.model.DataAncestorFKColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.PrimaryKeyConstraint;
import org.openforis.collect.relational.model.ReferentialConstraint;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.Table;

/**
 * 
 * @author S. Ricci
 * @author A. SanchezPaus Diaz
 *
 */
public class JooqRelationalSchemaCreator implements RelationalSchemaCreator {

	@Override
	public void createRelationalSchema(RelationalSchema schema, Connection conn)
			throws CollectRdbException {
		CollectDSLContext dsl = new CollectDSLContext(conn);
		
		for (Table<?> table : schema.getTables()) {
			org.jooq.Table<Record> jooqTable = jooqTable(schema, table, ! dsl.isSchemaLess());
			CreateTableAsStep<Record> createTableStep = dsl.createTable(jooqTable);
			Query createTableFinalQuery = (Query) createTableStep;
			for (Column<?> column : table.getColumns()) {
				DataType<?> dataType = dsl.getDataType(column.getType().getJavaType());
				Integer length = column.getLength();
				if (length != null) {
					dataType.length(length);
				}
				createTableFinalQuery = createTableStep.column(column.getName(), dataType);
			}
			createTableFinalQuery.execute();
		}
		
		createDataTableViews(schema, conn);
	}
	
	@Override
	public void addConstraints(RelationalSchema schema, Connection conn) {
		CollectDSLContext dsl = new CollectDSLContext(conn);
		
		if (dsl.isSQLite()) {
			//cannot add PK constraints after table creation
		} else {
			addPKConstraints(schema, dsl);
		}
		if (dsl.isForeignKeySupported()) {
			createForeignKeys(schema, dsl);
		}
	}

	@Override
	public void addIndexes(RelationalSchema schema, Connection conn) {
		CollectDSLContext dsl = new CollectDSLContext(conn);
		if ( dsl.isSQLite()) {
			addCodeListsCodeIndexes(schema, dsl);
			addPKIndexes(schema, dsl);
			addFKIndexes(schema, dsl);
		} else {
			//for other DBMS, indexes are already created together with PK and FK constraints
		}
	}

	private void createDataTableViews(RelationalSchema schema, Connection conn) {
		List<DataTable> dataTables = schema.getDataTables();
		for (DataTable dataTable : dataTables) {
			createDataTableView(schema, dataTable, conn);
		}
	}

	private void createDataTableView(RelationalSchema schema, DataTable dataTable, Connection conn) {
		CollectDSLContext dsl = new CollectDSLContext(conn);
		List<Field<?>> fields = new ArrayList<Field<?>>();
		List<TableLike<?>> tables = new ArrayList<TableLike<?>>();
		List<Condition> conditions = new ArrayList<Condition>();
		DataTable currentTable = dataTable;
		while (currentTable != null) {
			org.jooq.Table<Record> currentJooqTable = jooqTable(schema, currentTable, ! dsl.isSchemaLess());
			tables.add(currentJooqTable);
			List<Column<?>> columns = currentTable.getColumns();
			for (Column<?> column : columns) {
				if (! (column instanceof DataAncestorFKColumn)) {
					fields.add(field(name(currentJooqTable.getName(), column.getName())));
				}
			}
			//add parent table join condition
			DataTable parentTable = currentTable.getParent();
			if (parentTable != null) {
				//names are duplicate, use the table name as prefix in the join condition
				Condition parentTableJoinCondition = field(currentJooqTable.getName() + "." + currentTable.getParentFKColumn().getName())
						.eq(field(parentTable.getName() + "." + parentTable.getPrimaryKeyColumn().getName()));
				conditions.add(parentTableJoinCondition);
			}
			currentTable = parentTable;
		}
		Select<?> select = dsl.select(fields).from(tables).where(conditions);
		Name name;
		if (dsl.isSchemaLess()) {
			name = name(dataTable.getName() + "_view");
		} else {
			name = name(schema.getName(), dataTable.getName() + "_view");
		}
		dsl.createView(DSL.table(name), 
				fields.toArray(new Field[fields.size()]))
			.as(select)
			.execute();
	}
	
	private void addCodeListsCodeIndexes(RelationalSchema schema, CollectDSLContext dsl) {
		for (Table<?> table : schema.getTables()) {
			if (table instanceof CodeTable) {
				CodeTable codeTable = (CodeTable) table;
				org.jooq.Table<Record> jooqTable = jooqTable(schema, table, ! dsl.isSchemaLess());
				CodeListCodeColumn codeColumn = codeTable.getCodeColumn();
				CollectCreateIndexStep createIndexStep = dsl.createIndex(table.getName() + "_code_idx");
				if (codeTable.getLevelIdx() == null || codeTable.getLevelIdx() == 0) { 
					//unique index only for first level or when the code scope is 'scheme'
					createIndexStep.unique();
				}
				createIndexStep
					.on(jooqTable, field(codeColumn.getName()))
					.execute();
			}
		}
	}

	private void addPKConstraints(RelationalSchema schema, CollectDSLContext dsl) {
		for (Table<?> table : schema.getTables()) {
			org.jooq.Table<Record> jooqTable = jooqTable(schema, table, ! dsl.isSchemaLess());
			PrimaryKeyConstraint pkConstraint = table.getPrimaryKeyConstraint();
			String pkColumnName = pkConstraint.getPrimaryKeyColumn().getName();
			String pkConstraintName = table.getName() + "_pk";
		
			dsl.alterTable(jooqTable)
				.add(constraint(pkConstraintName)
					.primaryKey(pkColumnName))
				.execute();
		}
	}
	
	private void addPKIndexes(RelationalSchema schema, CollectDSLContext dsl) {
		for (Table<?> table : schema.getTables()) {
			org.jooq.Table<Record> jooqTable = jooqTable(schema, table, ! dsl.isSchemaLess());
			//For SQLite it creates an index on the primary key, for POSTGRESQL it will alter the table to set the primary key columns
			PrimaryKeyConstraint pkConstraint = table.getPrimaryKeyConstraint();
			String pkColumnName = pkConstraint.getPrimaryKeyColumn().getName();
			String pkConstraintName = table.getName() + "_pk";
			dsl.createIndex(pkConstraintName)
				.unique()
				.on(jooqTable, field(pkColumnName))
				.execute();
		}
	}
	
	private void addFKIndexes(RelationalSchema schema, CollectDSLContext dsl) {
		for (Table<?> table : schema.getTables()) {
			if (table instanceof DataTable) {
				org.jooq.Table<Record> jooqTable = jooqTable(schema, table, ! dsl.isSchemaLess());
				int idxCount = 1;
				for (ReferentialConstraint referentialConstraint : table.getReferentialContraints()) {
					String idxName = String.format("%s_%d_idx", table.getName(), idxCount);
					dsl.createIndex(idxName)
						.on(jooqTable, toJooqFields(referentialConstraint.getColumns()))
						.execute();
					idxCount ++;
				}
			}
		}
	}

	private void createForeignKeys(RelationalSchema schema, CollectDSLContext dsl) {
		for (org.openforis.collect.relational.model.Table<?> table : schema.getTables()) {
			List<ReferentialConstraint> fks = table.getReferentialContraints();
			for (ReferentialConstraint fk : fks) {
				Field<?>[] fields = toJooqFields(fk.getColumns());
				org.jooq.Table<Record> jooqTable = jooqTable(schema, table, ! dsl.isSchemaLess());
				org.jooq.Table<Record> referencedJooqTable = jooqTable(schema, fk.getReferencedKey().getTable(), ! dsl.isSchemaLess());
				List<Column<?>> referencedColumns = fk.getReferencedKey().getColumns();
				dsl.alterTable(jooqTable)
					.add(constraint(fk.getName())
							.foreignKey(fields)
							.references(referencedJooqTable, toJooqFields(referencedColumns)))
					.execute();
			}
		}
	}

	private Field<?>[] toJooqFields(List<Column<?>> columns) {
		List<Field<?>> fields = new ArrayList<Field<?>>(columns.size());
		for (Column<?> column : columns) {
			fields.add(field(column.getName()));
		}
		return fields.toArray(new Field<?>[fields.size()]);
	}

	private org.jooq.Table<Record> jooqTable(RelationalSchema schema,
			org.openforis.collect.relational.model.Table<?> table, boolean renderSchema) {
		if (renderSchema) {
			return table(name(schema.getName(), table.getName()));
		} else {
			return table(name(table.getName()));
		}
	}

}
