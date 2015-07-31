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
import org.openforis.collect.relational.model.PrimaryKeyColumn;
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
					//TODO use Jooq to add primary key constraint at table creation time (not supported yet)
				}
				createTableFinalQuery = columnStep;
			}
			createTableFinalQuery.execute();

			//TODO add primary key constraint ?
//			PrimaryKeyConstraint pkConstraint = table.getPrimaryKeyConstraint();
//			String pkColumnName = pkConstraint.getPrimaryKeyColumn().getName();
//			dsl.alterTable(jooqTable).add(
//					constraint(table.getName() + "_pk").primaryKey(pkColumnName)
//				).execute();
			
			Column<?> pkColumn = table.getPrimaryKeyConstraint().getPrimaryKeyColumn();
			dsl.createIndex(table.getName() + "_pk")
				.on(jooqTable, field(pkColumn.getName()))
				.execute();
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
					.add(constraint(fk.getName())
							.foreignKey(fields)
							.references(referencedTableName, referencedColumnNames))
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
