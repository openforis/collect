package org.openforis.collect.relational.mondrian;

import java.util.List;

import mondrian.olap.MondrianDef;
import mondrian.olap.MondrianDef.Attribute;
import mondrian.olap.MondrianDef.Attributes;
import mondrian.olap.MondrianDef.Column;
import mondrian.olap.MondrianDef.Cube;
import mondrian.olap.MondrianDef.Dimension;
import mondrian.olap.MondrianDef.DimensionLinks;
import mondrian.olap.MondrianDef.Dimensions;
import mondrian.olap.MondrianDef.Key;
import mondrian.olap.MondrianDef.Measure;
import mondrian.olap.MondrianDef.MeasureGroup;
import mondrian.olap.MondrianDef.MeasureGroups;
import mondrian.olap.MondrianDef.Measures;
import mondrian.olap.MondrianDef.NoLink;
import mondrian.olap.MondrianDef.PhysicalSchema;
import mondrian.rolap.RolapAggregator;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.model.DataColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.PrimaryKeyConstraint;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;

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
		schema.metamodelVersion = "4.0";
		schema.name = survey.getName();
		PhysicalSchema physicalSchema = generatePhysicalSchema();
		schema.children.add(physicalSchema);
		
		List<EntityDefinition> rootEntityDefinitions = survey.getSchema().getRootEntityDefinitions();
		for (EntityDefinition rootEntityDef : rootEntityDefinitions) {
			final DataTable dataTable = rdbSchema.getDataTable(rootEntityDef);

			Cube cube = new Cube();
			cube.name = rootEntityDef.getName();
			MeasureGroups measureGroups = new MondrianDef.MeasureGroups();
			MeasureGroup measureGroup = new MeasureGroup();
			measureGroup.name = cube.getName();
			Measures measures = new Measures();
			Measure measure = new Measure();
			measure.name = rootEntityDef.getName() + " count";
			measure.aggregator = RolapAggregator.Count.name;
			measure.table = dataTable.getName();
			measures.list().add(measure);
			measureGroup.children.add(measures);
			measureGroup.table = dataTable.getName();
			DimensionLinks dimensionLinks = new DimensionLinks();
			measureGroup.children.add(dimensionLinks);
			measureGroups.list().add(measureGroup);
			cube.children.add(measureGroups);
			
			final Dimensions dimensions = new Dimensions();
			
			List<NodeDefinition> childDefinitions = rootEntityDef.getChildDefinitions();
			for (NodeDefinition def : childDefinitions) {
				if (def instanceof AttributeDefinition) {
					AttributeDefinition attrDef = (AttributeDefinition) def;
					Dimension dimension = new Dimension();
					dimension.name = def.getName();
					Attributes attributes = new Attributes();
					Attribute attribute = new Attribute();
					attribute.name = def.getName();
					attribute.table = dataTable.getName();
					List<DataColumn> dataColumns = dataTable.getDataColumns(attrDef);
					Key attributeKey = new Key();
					attributeKey.name = attribute.name + "_key";
					for (DataColumn col : dataColumns) {
						Column keyCol = new Column();
						keyCol.table = dataTable.getName();
						keyCol.name = col.getName();
						attributeKey.list().add(keyCol);
					}
					attribute.children.add(attributeKey);
					attributes.list().add(attribute);
					dimension.children.add(attributes);
					dimensions.list().add(dimension);
					//add dimension link
					NoLink dimensionLink = new NoLink();
					dimensionLink.dimension = dimension.name;
					dimensionLinks.list().add(dimensionLink);
				}
			}
			cube.children.add(dimensions);
			
			schema.children.add(cube);
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
