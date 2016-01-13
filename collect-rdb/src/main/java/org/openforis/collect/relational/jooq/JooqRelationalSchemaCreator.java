package org.openforis.collect.relational.jooq;

import static org.jooq.impl.DSL.constraint;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.jooq.CreateTableAsStep;
import org.jooq.CreateTableColumnStep;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.impl.DefaultDataType;
import org.openforis.collect.persistence.jooq.CollectDSLContext;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.RelationalSchemaCreator;
import org.openforis.collect.relational.model.Column;
import org.openforis.collect.relational.model.DataTable;
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
				
				createTableFinalQuery = columnStep;
			}
			createTableFinalQuery.execute();

			//For SQLite it creates an index on the primary key, for POSTGRESQL it will alter the table to set the primary key columns
			if ( dsl.isSQLite()) {
				Column<?> pkColumn = table.getPrimaryKeyConstraint().getPrimaryKeyColumn();
				dsl.createIndex(table.getName() + "_pk")
					.on(jooqTable, field(pkColumn.getName()))
					.execute();
			} else {
				PrimaryKeyConstraint pkConstraint = table.getPrimaryKeyConstraint();
				String pkColumnName = pkConstraint.getPrimaryKeyColumn().getName();
				dsl.alterTable(jooqTable).add(
						constraint(table.getName() + "_pk").primaryKey(pkColumnName)
					).execute();
			}
			
			if (table instanceof DataTable) {
				int count = 1;
				List<ReferentialConstraint> referentialContraints = table.getReferentialContraints();
				for (ReferentialConstraint referentialConstraint : referentialContraints) {
					createIndex(dsl, count++, schema, table, referentialConstraint.getColumns());
				}
			}
		}
		if(dsl.isForeignKeySupported()) {
			createForeignKeys(schema, dsl);
		}
	}

	private void createIndex(CollectDSLContext dsl, int indexNum, RelationalSchema schema, 
			Table<?> table, List<Column<?>> columns) {
		org.jooq.Table<Record> jooqTable = createJooqTable(schema, table, ! dsl.isSchemaLess());
		String name = String.format("%s_%d_idx", table.getName(), indexNum);
		dsl.createIndex(name)
			.on(jooqTable, toJooqFields(columns))
			.execute();
	}

	private void createForeignKeys(RelationalSchema schema, CollectDSLContext dsl) {
		for (org.openforis.collect.relational.model.Table<?> table : schema.getTables()) {
			List<ReferentialConstraint> fks = table.getReferentialContraints();
			for (ReferentialConstraint fk : fks) {
				Field<?>[] fields = toJooqFields(fk.getColumns());
				org.jooq.Table<Record> jooqTable = createJooqTable(schema, table, ! dsl.isSchemaLess());
				org.jooq.Table<Record> referencedJooqTable = createJooqTable(schema, fk.getReferencedKey().getTable(), ! dsl.isSchemaLess());
				List<Column<?>> referencedColumns = fk.getReferencedKey().getColumns();
				dsl.alterTable(jooqTable)
					.add(constraint(fk.getName())
							.foreignKey(fields)
							.references(referencedJooqTable, toJooqFields(referencedColumns)))
					.execute();
			}
		}
	}

	private Field<?>[] toJooqFields(List<Column<?>> fkColumns) {
		List<Field<?>> fields = new ArrayList<Field<?>>(fkColumns.size());
		for (Column<?> fkColumn : fkColumns) {
			fields.add(field(fkColumn.getName()));
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
			return table(name(schema.getName(), table.getName()));
		} else {
			return table(name(table.getName()));
		}
	}

}
