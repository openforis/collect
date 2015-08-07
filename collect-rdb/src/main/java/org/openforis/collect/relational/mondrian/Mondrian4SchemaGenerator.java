package org.openforis.collect.relational.mondrian;

import java.util.Arrays;
import java.util.List;

import mondrian.olap.Cube;
import mondrian.olap.MondrianDef;
import mondrian.olap.MondrianDef.Column;
import mondrian.olap.MondrianDef.Key;
import mondrian.olap.MondrianDef.PhysicalSchema;
import mondrian.olap.MondrianDef.Table;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.RelationalSchemaCreator;
import org.openforis.collect.relational.jooq.JooqRelationalSchemaCreator;
import org.openforis.collect.relational.model.PrimaryKeyConstraint;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.idm.metamodel.EntityDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
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
	
	public MondrianDef.Schema generateSchema() {
		MondrianDef.Schema schema = new MondrianDef.Schema();
		PhysicalSchema physicalSchema = generatePhysicalSchema();
		schema.children.add(physicalSchema);
		
		List<EntityDefinition> rootEntityDefinitions = survey.getSchema().getRootEntityDefinitions();
		for (EntityDefinition rootEntityDef : rootEntityDefinitions) {
			MondrianDef.Cube cube = new MondrianDef.Cube();
			cube.name = rootEntityDef.getName();
//			cube.getDimensions()
		}
		return schema;
	}

	private PhysicalSchema generatePhysicalSchema() {
		PhysicalSchema physicalSchema = new PhysicalSchema();
		for (org.openforis.collect.relational.model.Table<?> table : rdbSchema.getTables()) {
			MondrianDef.Table mondrianTable = new MondrianDef.Table();
			mondrianTable.name = table.getName();
			PrimaryKeyConstraint pkConstraint = table.getPrimaryKeyConstraint();
			mondrianTable.keyColumn = pkConstraint.getPrimaryKeyColumn().getName();
			//TODO Foreign Keys
			physicalSchema.children.add(mondrianTable);
		}
		return physicalSchema;
	}
	
}
