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

	private static final String VIEW_SUFFIX = "_view";

	protected Connection conn;
	protected CollectDSLContext dsl;
	protected RelationalSchema schema;
			
	public JooqRelationalSchemaCreator(RelationalSchema schema, Connection conn) {
		super();
		this.schema = schema;
		this.conn = conn;
		dsl = new CollectDSLContext(conn);
	}

	@Override
	public void createRelationalSchema() throws CollectRdbException {
		for (Table<?> table : schema.getTables()) {
			org.jooq.Table<Record> jooqTable = jooqTable(table);
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
		
		createDataTableViews();
	}
	
	@Override
	public void addConstraints() {
		if (dsl.isSQLite()) {
			//cannot add PK constraints after table creation
		} else {
			addPKConstraints();
		}
		if (dsl.isForeignKeySupported()) {
			createForeignKeys();
		}
	}

	@Override
	public void addIndexes() {
		if ( dsl.isSQLite()) {
			addCodeListsCodeIndexes();
			addPKIndexes();
			addFKIndexes();
		} else {
			//for other DBMS, indexes are already created together with PK and FK constraints
		}
	}

	private void createDataTableViews() {
		List<DataTable> dataTables = schema.getDataTables();
		for (DataTable dataTable : dataTables) {
			createDataTableView(dataTable);
		}
	}

	private void createDataTableView(DataTable dataTable) {
		List<Field<?>> fields = new ArrayList<Field<?>>();
		List<TableLike<?>> tables = new ArrayList<TableLike<?>>();
		List<Condition> conditions = new ArrayList<Condition>();
		DataTable currentTable = dataTable;
		while (currentTable != null) {
			org.jooq.Table<Record> currentJooqTable = jooqTable(currentTable);
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
		Name name = getDataTableViewName(schema, dataTable);
		dsl.createView(DSL.table(name), 
				fields.toArray(new Field[fields.size()]))
			.as(select)
			.execute();
	}

	private Name getDataTableViewName(RelationalSchema schema, DataTable dataTable) {
		String dataTableName = dataTable.getName();
		if (dsl.isSchemaLess()) {
			return name(getDataTableViewName(dataTableName));
		} else {
			return name(schema.getName(), getDataTableViewName(dataTableName));
		}
	}

	public String getDataTableViewName(String dataTableName) {
		return dataTableName + VIEW_SUFFIX;
	}
	
	private void addCodeListsCodeIndexes() {
		for (Table<?> table : schema.getTables()) {
			if (table instanceof CodeTable) {
				CodeTable codeTable = (CodeTable) table;
				org.jooq.Table<Record> jooqTable = jooqTable(table);
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

	private void addPKConstraints() {
		for (Table<?> table : schema.getTables()) {
			org.jooq.Table<Record> jooqTable = jooqTable(table);
			PrimaryKeyConstraint pkConstraint = table.getPrimaryKeyConstraint();
			String pkColumnName = pkConstraint.getPrimaryKeyColumn().getName();
			String pkConstraintName = table.getName() + "_pk";
		
			dsl.alterTable(jooqTable)
				.add(constraint(pkConstraintName)
					.primaryKey(pkColumnName))
				.execute();
		}
	}
	
	private void addPKIndexes() {
		for (Table<?> table : schema.getTables()) {
			org.jooq.Table<Record> jooqTable = jooqTable(table);
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
	
	private void addFKIndexes() {
		for (Table<?> table : schema.getTables()) {
			if (table instanceof DataTable) {
				org.jooq.Table<Record> jooqTable = jooqTable(table);
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

	private void createForeignKeys() {
		for (org.openforis.collect.relational.model.Table<?> table : schema.getTables()) {
			List<ReferentialConstraint> fks = table.getReferentialContraints();
			for (ReferentialConstraint fk : fks) {
				Field<?>[] fields = toJooqFields(fk.getColumns());
				org.jooq.Table<Record> jooqTable = jooqTable(table);
				org.jooq.Table<Record> referencedJooqTable = jooqTable(fk.getReferencedKey().getTable());
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

	protected org.jooq.Table<Record> jooqTable(
			org.openforis.collect.relational.model.Table<?> table) {
		if (! dsl.isSchemaLess()) {
			return table(name(schema.getName(), table.getName()));
		} else {
			return table(name(table.getName()));
		}
	}

}
