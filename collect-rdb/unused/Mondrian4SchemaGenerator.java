package org.openforis.collect.relational.mondrian;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.model.CodeTable;
import org.openforis.collect.relational.model.CodeValueFKColumn;
import org.openforis.collect.relational.model.DataColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.PrimaryKeyConstraint;
import org.openforis.collect.relational.model.ReferentialConstraint;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.collect.relational.model.UniquenessConstraint;
import org.openforis.collect.relational.util.CodeListTables;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel;
import org.openforis.idm.metamodel.TimeAttributeDefinition;

import com.thoughtworks.xstream.XStream;

import mondrian.olap.MondrianDef;
import mondrian.olap.MondrianDef.Attribute;
import mondrian.olap.MondrianDef.Attributes;
import mondrian.olap.MondrianDef.Column;
import mondrian.olap.MondrianDef.Cube;
import mondrian.olap.MondrianDef.Dimension;
import mondrian.olap.MondrianDef.DimensionLink;
import mondrian.olap.MondrianDef.DimensionLinks;
import mondrian.olap.MondrianDef.Dimensions;
import mondrian.olap.MondrianDef.FactLink;
import mondrian.olap.MondrianDef.ForeignKey;
import mondrian.olap.MondrianDef.ForeignKeyLink;
import mondrian.olap.MondrianDef.Hierarchies;
import mondrian.olap.MondrianDef.Hierarchy;
import mondrian.olap.MondrianDef.Level;
import mondrian.olap.MondrianDef.Link;
import mondrian.olap.MondrianDef.Measure;
import mondrian.olap.MondrianDef.MeasureGroup;
import mondrian.olap.MondrianDef.MeasureGroups;
import mondrian.olap.MondrianDef.Measures;
import mondrian.olap.MondrianDef.NoLink;
import mondrian.olap.MondrianDef.PhysicalSchema;
import mondrian.olap.MondrianDef.Table;
import mondrian.rolap.RolapAggregator;

/**
 * 
 * @author S. Ricci
 *
 */
public class Mondrian4SchemaGenerator {

	private static final String VERSION_4_0 = "4.0";
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
	
	public String generateXMLSchema() {
		MondrianDef.Schema schema = generateSchema();
		XStream xStream = new XStream();
		xStream.processAnnotations(MondrianDef.Schema.class);
		String xmlSchema = xStream.toXML(schema);
		return xmlSchema;
	}

	public MondrianDef.Schema generateSchema() {
		MondrianDef.Schema schema = new MondrianDef.Schema();
		schema.metamodelVersion = VERSION_4_0;
		schema.name = survey.getName();
		PhysicalSchema physicalSchema = generatePhysicalSchema();
		schema.children.add(physicalSchema);
		
		for (DataTable dataTable : rdbSchema.getDataTables()) {
			schema.children.add(createCube(dataTable));
		}
		return schema;
	}
	
	private Cube createCube(DataTable dataTable) {
		NodeDefinition nodeDef = dataTable.getNodeDefinition();

		Cube cube = new Cube();
		cube.name = nodeDef.getName();
		MeasureGroups measureGroups = new MondrianDef.MeasureGroups();
		MeasureGroup measureGroup = new MeasureGroup();
		measureGroup.name = cube.name;
		Measures measures = new Measures();
		List<Measure> measureList = createMeasures(dataTable);
		measures.list().addAll(measureList);
		measureGroup.children.add(measures);
		measureGroup.table = dataTable.getName();
		DimensionLinks dimensionLinks = new DimensionLinks();
		measureGroup.children.add(dimensionLinks);
		measureGroups.list().add(measureGroup);
		cube.children.add(measureGroups);
		
		if (nodeDef instanceof EntityDefinition) {
			Dimensions dimensions = new Dimensions();
			Queue<NodeDefinition> queue = new LinkedList<NodeDefinition>();
			queue.addAll(((EntityDefinition) nodeDef).getChildDefinitions());
			while (! queue.isEmpty()) {
				NodeDefinition def = queue.poll();
				if (def instanceof AttributeDefinition) {
					AttributeDefinition attrDef = (AttributeDefinition) def;
					Dimension dimension = createDimension(dataTable, attrDef);
					if (dimension != null) {
						dimensions.list().add(dimension);
						//add dimension link
						DimensionLink dimensionLink = createDimensionLink(dimension, attrDef);
						dimensionLinks.list().add(dimensionLink);
					}
				} else if (! def.isMultiple()) {
					queue.addAll(((EntityDefinition) def).getChildDefinitions());
				}
			}
			cube.children.add(dimensions);
		}
		return cube;
	}

	private List<Measure> createMeasures(DataTable dataTable) {
		List<Measure> result = new ArrayList<Measure>();
		Measure measure = new Measure();
		measure.name = dataTable.getNodeDefinition().getName() + " count";
		measure.column = dataTable.getPrimaryKeyColumn().getName();
		measure.aggregator = RolapAggregator.DistinctCount.name;
		measure.table = dataTable.getName();
		result.add(measure);
		return result;
	}

	private DimensionLink createDimensionLink(Dimension dimension, AttributeDefinition attrDef) {
		DimensionLink dimensionLink;
		if (attrDef instanceof CodeAttributeDefinition) {
			CodeAttributeDefinition codeAttrDef = (CodeAttributeDefinition) attrDef;
			if (codeAttrDef.isMultiple()) {
				dimensionLink = new NoLink();
			} else if (codeAttrDef.getList().isExternal()) {
				dimensionLink = new FactLink();
			} else {
				dimensionLink = new ForeignKeyLink();
				DataTable dataTable = rdbSchema.getDataTable(attrDef.getParentEntityDefinition());
				CodeValueFKColumn fkColumn = dataTable.getForeignKeyCodeColumn((CodeAttributeDefinition) attrDef);
				((ForeignKeyLink) dimensionLink).foreignKeyColumn = fkColumn.getName();
			}
		} else {
			dimensionLink = new FactLink();
		}
		dimensionLink.dimension = dimension.name;
		return dimensionLink;
	}

	private Dimension createDimension(DataTable dataTable, AttributeDefinition attrDefn) {
		List<Attribute> attrs = createDimensionAttributes(dataTable, attrDefn);
		if (attrs.isEmpty()) {
			return null;
		} else {
			Dimension dimension = new Dimension();
			dimension.name = attrDefn.getName();
			dimension.caption = getDimensionCaption(attrDefn);
			if (attrDefn.hasMainField()) {
				dimension.key = attrDefn.getMainFieldName();
			} else {
				dimension.key = attrDefn.getName();
			}
			dimension.table = dataTable.getName();
			
			Attributes attributes = new Attributes();
			attributes.list().addAll(attrs);
			dimension.children.add(attributes);
			
			List<Level> hierarchyLevels = createHierarchyLevels(attrDefn);
			if (! hierarchyLevels.isEmpty()) {
				Hierarchies hierarchies = new Hierarchies();
				Hierarchy hierarchy = new Hierarchy();
				hierarchy.name = attrDefn.getName() + "_full_hierarchy";
				hierarchy.children.addAll(hierarchyLevels);
				hierarchies.list().add(hierarchy);
				dimension.children.add(hierarchies);
			}
			return dimension;
		}
	}

	private List<Attribute> createDimensionAttributes(DataTable dataTable,
			AttributeDefinition attrDefn) {
		List<Attribute> attributes = new ArrayList<Attribute>();
		
		if (attrDefn instanceof CodeAttributeDefinition && ! ((CodeAttributeDefinition) attrDefn).getList().isExternal()) {
			CodeAttributeDefinition codeAttrDefn = (CodeAttributeDefinition) attrDefn;
			CodeTable codeListTable = rdbSchema.getCodeListTable(codeAttrDefn);
			String codeListTableName = codeListTable.getName();
			Attribute attribute = new Attribute();
			FieldDefinition<String> codeFieldDef = codeAttrDefn.getCodeFieldDefinition();
			attribute.name = codeFieldDef.getName();
			attribute.caption = getAttributeCaption(codeFieldDef);
			attribute.table = codeListTableName;
			attribute.keyColumn = CodeListTables.getCodeColumnName(rdbConfig, codeListTableName);
			attribute.nameColumn = CodeListTables.getLabelColumnName(rdbConfig, codeListTableName);
			attributes.add(attribute);
		} else if (attrDefn.hasMainField()) {
			attributes.addAll(createAttributesForFields(dataTable, attrDefn));
		} else if (attrDefn instanceof DateAttributeDefinition || attrDefn instanceof TimeAttributeDefinition) {
			List<DataColumn> dataColumns = dataTable.getDataColumns(attrDefn);
			DataColumn dataColumn = dataColumns.get(0);
			Attribute attribute = new Attribute();
			attribute.name = attrDefn.getName();
			attribute.caption = getDimensionCaption(attrDefn);
			attribute.keyColumn = dataColumn.getName();
			attributes.add(attribute);
			attributes.addAll(createAttributesForFields(dataTable, attrDefn));
		} else {
			//TODO
			// every field makes the Key of the Attribute ?! then nameColumn must be specified
//			Attribute attribute = new Attribute();
//			attribute.name = attrDefn.getName();
//			attribute.caption = getDimensionCaption(attrDefn);
//			Key key = new Key();
//			for (FieldDefinition<?> fieldDef : attrDefn.getFieldDefinitions()) {
//				DataColumn dataColumn = dataTable.getDataColumn(fieldDef);
//				if (dataColumn != null) {
//					Column column = new Column();
//					column.name = dataColumn.getName();
//					key.list().add(column);
//				}
//			}
//			attribute.children.add(key);
//			Name name = new Name();
//			name.list().add(e)
//			attribute.children.add(name);
//			attributes.add(attribute);
		}
		return attributes;
	}

	private List<Attribute> createAttributesForFields(DataTable dataTable,
			AttributeDefinition attrDefn) {
		List<FieldDefinition<?>> fieldDefs = attrDefn.getFieldDefinitions();
		
		List<Attribute> attributes = new ArrayList<Attribute>(fieldDefs.size());
		
		for (FieldDefinition<?> fieldDef : fieldDefs) {
			DataColumn col = dataTable.getDataColumn(fieldDef);
			if (col != null) {
				Attribute attribute = new Attribute();
				String fieldName = fieldDef.getName();
				attribute.name = fieldName;
				attribute.caption = getAttributeCaption(fieldDef);
				attribute.keyColumn = col.getName();
//				if (attrDefn instanceof DateAttributeDefinition) {
//					attribute.levelType = getDateFieldLevelType(fieldName);
//				}
				attributes.add(attribute);
			}
		}
		return attributes;
	}

	private List<Level> createHierarchyLevels(
			AttributeDefinition attrDef) {
		List<Level> levels = new ArrayList<Level>();
//		if (attrDef instanceof DateAttributeDefinition) {
//			for (FieldDefinition<?> fieldDef : attrDef.getFieldDefinitions()) {
//				String fieldName = fieldDef.getName();
//				Level level = new Level();
//				level.name = level.attribute = fieldName;
//				levels.add(level);
//			}
//		} else {
//			
//		}
		return levels;
	}

//	private String getDateFieldLevelType(String fieldName) {
//		if (DateAttributeDefinition.YEAR_FIELD_NAME.equals(fieldName)) {
//			return "TimeYears";
//		} else if (DateAttributeDefinition.MONTH_FIELD_NAME.equals(fieldName)) {
//			return "TimeMonths";
//		} else if(DateAttributeDefinition.DAY_FIELD_NAME.equals(fieldName)) {
//			return "TimeDays";
//		} else {
//			throw new IllegalArgumentException("Unexpected date attribute field name: " + fieldName);
//		}
//	}

	private PhysicalSchema generatePhysicalSchema() {
		PhysicalSchema physicalSchema = new PhysicalSchema();
		for (org.openforis.collect.relational.model.Table<?> rdbTable : rdbSchema.getTables()) {
			Table table = new Table();
			table.name = rdbTable.getName();
			PrimaryKeyConstraint pkConstraint = rdbTable.getPrimaryKeyConstraint();
			table.keyColumn = pkConstraint.getPrimaryKeyColumn().getName();
			physicalSchema.children.add(table);
			
			//add foreign keys
			for (ReferentialConstraint referentialConstraint : rdbTable.getReferentialContraints()) {
				UniquenessConstraint referencedKey = referentialConstraint.getReferencedKey();
				org.openforis.collect.relational.model.Table<?> referencedRdbTable = referencedKey.getTable();
				Link link = new Link();
				link.source = rdbTable.getName();
				link.target = referencedRdbTable.getName();
				ForeignKey foreignKey = new ForeignKey();
				for (org.openforis.collect.relational.model.Column<?> referencedRdbColumn : referencedKey.getColumns()) {
					Column fkColumn = new Column();
					fkColumn.name = referencedRdbColumn.getName();
					foreignKey.list().add(fkColumn);
				}
				link.foreignKey = foreignKey;
				physicalSchema.children.add(link);
			}
		}
		return physicalSchema;
	}
	
	private String extractLabel(NodeDefinition nodeDef) {
		String attrLabel = nodeDef.getLabel(NodeLabel.Type.INSTANCE);
		if (attrLabel == null) {
			attrLabel = nodeDef.getName();
		}
		return attrLabel;
	}
	
	private String getDimensionCaption(AttributeDefinition attrDefn) {
		return String.format("%s [%s]", extractLabel(attrDefn), attrDefn.getName());
	}

	private String getAttributeCaption(FieldDefinition<?> fieldDef) {
		AttributeDefinition attrDef = fieldDef.getAttributeDefinition();
		String caption = extractLabel(attrDef);
		if (! (attrDef.hasMainField() && attrDef.getMainFieldDefinition() == fieldDef)) {
			caption += " - " + fieldDef.getName();
		}
		return caption;
	}

}
