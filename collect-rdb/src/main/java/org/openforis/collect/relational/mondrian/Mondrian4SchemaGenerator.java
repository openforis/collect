package org.openforis.collect.relational.mondrian;

import java.util.Arrays;
import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.RelationalSchemaCreator;
import org.openforis.collect.relational.jooq.JooqRelationalSchemaCreator;
import org.openforis.collect.relational.model.PrimaryKeyConstraint;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.collect.relational.mondrian4.Column;
import org.openforis.collect.relational.mondrian4.Key;
import org.openforis.collect.relational.mondrian4.PhysicalSchema;
import org.openforis.collect.relational.mondrian4.Table;

public class Mondrian4SchemaGenerator {

	private CollectSurvey survey;
	private RelationalSchemaConfig rdbConfig;
	private RelationalSchema rdbSchema;

	public Mondrian4SchemaGenerator(CollectSurvey survey, RelationalSchemaConfig rdbConfig) {
		this.survey = survey;
		this.rdbConfig = rdbConfig;
		
		init();
	}

	private void init() {
		RelationalSchemaGenerator schemaGenerator = new RelationalSchemaGenerator(rdbConfig);
		rdbSchema = schemaGenerator.generateSchema(survey, survey.getName());
	}
	
	public PhysicalSchema generateSchema() {
		PhysicalSchema schema = new PhysicalSchema();
		for (org.openforis.collect.relational.model.Table<?> table : rdbSchema.getTables()) {
			Table mondrianTable = new Table();
			mondrianTable.setName(table.getName());
			PrimaryKeyConstraint pkConstraint = table.getPrimaryKeyConstraint();
//			mondrianTable.setKeyColumn(pkConstraint.getPrimaryKeyColumn().getName());
			if (pkConstraint != null) {
				Key key = new Key();
				for (org.openforis.collect.relational.model.Column<?> pkColumn : pkConstraint.getColumns()) {
					Column mondrianPkColumn = new Column();
					mondrianPkColumn.setName(pkColumn.getName());
					key.getColumn().add(mondrianPkColumn);
				}
				mondrianTable.getTableElement().add(key);
			}
//			schema.getPhysicalSchemaElement().add(mondrianTable);
		}
		return schema;
	}
	
}
