package org.openforis.collect.relational.mondrian;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.collect.relational.mondrian.Hierarchy.Join;
import org.openforis.collect.relational.mondrian.Hierarchy.Level;
import org.openforis.collect.relational.mondrian.Schema.Cube;
import org.openforis.collect.relational.mondrian.Schema.Cube.Measure;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.KeyAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition.Type;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus Diaz
 *
 */
public class MondrianSchemaGenerator {
	
	private static final String[] MEASURE_AGGREGATORS = new String[] {"min", "max", "avg"};
	
	private CollectSurvey survey;
	private RelationalSchemaConfig rdbConfig;

	public MondrianSchemaGenerator(CollectSurvey survey, RelationalSchemaConfig rdbConfig) {
		this.survey = survey;
		this.rdbConfig = rdbConfig;
	}
	
	public Schema generateSchema() {
		Schema schema = new Schema();
		schema.setName(survey.getName());
		schema.cube = Arrays.asList(generateCube());
		return schema;
	}
	
//	public String generateXMLSchema() {
//		Schema mondrianSchema = generateSchema();
//		XStream xStream = new XStream();
//		xStream.processAnnotations(MondrianCubeGenerator.Schema.class);
//		String xmlSchema = xStream.toXML(mondrianSchema);
//		return xmlSchema;
//	}

	private Cube generateCube() {
		Cube cube = new Cube();
		EntityDefinition rootEntityDef = survey.getSchema().getRootEntityDefinitions().get(0);
		String rootEntityName = rootEntityDef.getName();
		cube.setName(rootEntityName);
		Table table = new Table();
		table.setName(rootEntityName);
		table.setSchema(survey.getName());
		cube.table = table;
		
		List<Measure> measures = new ArrayList<Measure>();
		List<Object> dimensions = new ArrayList<Object>();
		
		List<NodeDefinition> children = rootEntityDef.getChildDefinitions();
		for (NodeDefinition nodeDef : children) {
			String nodeName = nodeDef.getName();
			if (nodeDef instanceof AttributeDefinition) {
				PrivateDimension dimension = generateDimension(nodeDef);
				
				if (nodeDef instanceof KeyAttributeDefinition && ((KeyAttributeDefinition) nodeDef).isKey()) {
					Measure measure = new Measure();
					measure.setName(rootEntityName + "_count");
					measure.column = "_" + rootEntityName + "_" + nodeName;
					measure.caption = StringEscapeUtils.escapeHtml4( extractLabel(rootEntityDef) + " Count" );
					measure.aggregator = "distinct count";
					measure.datatype = "Integer";
					measures.add(measure);
				} else if (nodeDef instanceof NumberAttributeDefinition) {
					for (String aggregator : MEASURE_AGGREGATORS) {
						Measure measure = new Measure();
						measure.setName(nodeName + "_" + aggregator);
						measure.column = nodeName;
						measure.caption = StringEscapeUtils.escapeHtml4( extractLabel(nodeDef) + " " + aggregator );
						measure.aggregator = aggregator;
						measure.datatype = "Integer";
						measures.add(measure);
					}
				} 
				dimensions.add(dimension);
			} else {
				String rootEntityIdColumnName = rdbConfig.getIdColumnPrefix() + rootEntityName + rdbConfig.getIdColumnSuffix();
				
				String entityName = nodeName;
				String entityLabel = extractLabel(nodeDef);
				
				for (NodeDefinition childDef : ((EntityDefinition) nodeDef).getChildDefinitions()) {
					String childLabel = entityLabel + " - " + extractLabel(childDef);
					PrivateDimension dimension = new PrivateDimension();
					dimension.setName(childLabel);
					dimension.setType("StandardDimension");
					
					Hierarchy hierarchy = new Hierarchy();
					hierarchy.setName(childLabel);
					
					if( nodeDef.isMultiple() ){
						dimension.foreignKey = rootEntityIdColumnName;
						hierarchy.primaryKey = rootEntityIdColumnName;
						hierarchy.primaryKeyTable = entityName;
						
						if (childDef instanceof CodeAttributeDefinition) {
							
							Join join = new Join();
							String codeListName = ((CodeAttributeDefinition) childDef).getList().getName();
							join.leftKey = childDef.getName() + rdbConfig.getCodeListTableSuffix() + rdbConfig.getIdColumnSuffix();
							join.rightKey = codeListName + rdbConfig.getCodeListTableSuffix() + rdbConfig.getIdColumnSuffix();
							
							join.setLeftAlias(entityName);
							join.setRightAlias(codeListName + rdbConfig.getCodeListTableSuffix()); 
							
							hierarchy.join = join;
						}						
						
						hierarchy.level = Arrays.asList(generateLevel(childDef));
						
						dimension.hierarchy = Arrays.asList(hierarchy);
						
					}else{
						dimension = generateDimension(childDef);
					}
					
					dimensions.add(dimension);
				}
			}
		}
		//add predefined dimensions
		dimensions.addAll(generatePredefinedDimensions());
		
		//add predefined measures
		measures.addAll(0, generatePredefinedMeasures());
		
		cube.dimensionUsageOrDimension = dimensions;
		cube.measure = measures;
		return cube;
	}
	
	private List<Measure> generatePredefinedMeasures() {
		List<Measure> measures = new ArrayList<Measure>();
//		//Expansion factor - Area
//		{
//			Measure measure = new Measure();
//			measure.setName("area");
//			measure.column = "expansion_factor";
//			measure.caption = "Area (HA)";
//			measure.aggregator = "sum";
//			measure.datatype = "Integer";
//			measure.formatString = "#,###";
//			measures.add(measure);
//		}
//		
//		// Plot weight
//		{
//			Measure measure = new Measure("plot_weight");
//			measure.column = "plot_weight";
//			measure.caption = "Plot Weight";
//			measure.aggregator = "sum";
//			measure.datatype = "Integer";
//			measure.formatString = "#,###";
//			measures.add(measure);
//		}
		
		return measures;
	}
	
	private List<PrivateDimension> generatePredefinedDimensions() {
		List<PrivateDimension> dimensions = new ArrayList<PrivateDimension>();
//		//Slope category
//		{
//			Dimension d = new Dimension("Slope category");
//			d.foreignKey = "slope_id";
//			d.highCardinality = "false";
//			Hierarchy h = new Hierarchy();
//			h.table = new Table("slope_category");
//			Level l = new Level("Slope_category");
//			l.table = "slope_category";
//			l.column = "slope_id";
//			l.nameColumn = "slope_caption";
//			l.type = "String";
//			l.levelType = "Regular";
//			l.uniqueMembers = "false";
//			h.levels.add(l);
//			d.hierarchy = h;
//			dimensions.add(d);
//		}
//		//Initial Land Use
//		{
//			Dimension d = new Dimension("Initial Land Use");
//			d.foreignKey = "dynamics_id";
//			d.highCardinality = "false";
//			Hierarchy h = new Hierarchy();
//			h.table = new Table("dynamics_category");
//			Level l = new Level("Initial_land_use");
//			l.table = "dynamics_category";
//			l.column = "dynamics_id";
//			l.nameColumn = "dynamics_caption";
//			l.type = "String";
//			l.levelType = "Regular";
//			l.uniqueMembers = "false";
//			l.hideMemberIf = "Never";
//			h.levels.add(l);
//			d.hierarchy = h;
//			dimensions.add(d);
//		}
//		//Elevation Range
//		{
//			Dimension d = new Dimension("Elevation range");
//			d.foreignKey = "elevation_id";
//			d.highCardinality = "false";
//			Hierarchy h = new Hierarchy();
//			h.table = new Table("elevation_category");
//			Level l = new Level("Elevation_range");
//			l.table = "elevation_category";
//			l.column = "elevation_id";
//			l.nameColumn = "elevation_caption";
//			l.type = "String";
//			l.levelType = "Regular";
//			l.uniqueMembers = "false";
//			l.hideMemberIf = "Never";
//			h.levels.add(l);
//			d.hierarchy = h;
//			dimensions.add(d);
//		}
		return dimensions;
	}

	private PrivateDimension generateDimension(NodeDefinition nodeDef) {
		String attrName = nodeDef.getName();
		String attrLabel = extractLabel(nodeDef);
		PrivateDimension dimension = new PrivateDimension();
		dimension.setName(nodeDef.getName());
		dimension.setCaption(attrLabel);
		dimension.setType("StandardDimension");
		
		Hierarchy hierarchy = new Hierarchy();
		
		if (nodeDef instanceof CodeAttributeDefinition) {
			CodeAttributeDefinition codeAttrDef = (CodeAttributeDefinition) nodeDef;
			String codeTableName = extractCodeListTableName(codeAttrDef);
			dimension.foreignKey = attrName + rdbConfig.getCodeListTableSuffix() + rdbConfig.getIdColumnSuffix();						
			hierarchy.table = new Table();
			hierarchy.table.name = codeTableName;
		}
		
		List<Level> levels = new ArrayList<Level>();
		if (nodeDef instanceof DateAttributeDefinition) {
			dimension.type = "";
//			hierarchy.type = "TimeDimension";
			hierarchy.allMemberName = "attrLabel";
			String[] levelNames = new String[] {"Year", "Month", "Day"};
			for (String levelName : levelNames) {
				Level level = new Level();
				level.setName(levelName);
				level.setCaption(attrLabel + " - " + levelName);
				level.column = nodeDef.getName() + "_" + levelName.toLowerCase();
				level.levelType = String.format("Time%ss", levelName);
				level.type = "Numeric";
				levels.add(level);
			}
		} else {
			Level level = generateLevel(nodeDef);
			levels.add(level);
		}
		hierarchy.level = levels;
		dimension.hierarchy = Arrays.asList(hierarchy);
		return dimension;
	}

	private String extractCodeListTableName(CodeAttributeDefinition codeAttrDef) {
		StringBuffer codeListName = new StringBuffer( codeAttrDef.getList().getName() );
		
		int levelIdx = codeAttrDef.getLevelIndex();
		if ( levelIdx != -1 ) {
			CodeList codeList = codeAttrDef.getList();
			List<CodeListLevel> codeHierarchy = codeList.getHierarchy();
			if( !codeHierarchy.isEmpty() ){
				CodeListLevel currentLevel = codeHierarchy.get(levelIdx);
				codeListName.append("_");
				codeListName.append(currentLevel.getName());
			}
		}
		return codeListName.append(rdbConfig.getCodeListTableSuffix()).toString();
	}
	
	private Level generateLevel(NodeDefinition nodeDef) {
		String attrName = nodeDef.getName();
		String attrLabel = extractLabel(nodeDef);
		Level level = new Level();
		level.setName(attrName);
		level.setCaption(attrLabel);
		if (nodeDef instanceof NumericAttributeDefinition) {
			level.type = ((NumericAttributeDefinition) nodeDef).getType() == Type.INTEGER ? "Integer": "Numeric";
		} else {
			level.type = "String";	
		}
		level.levelType = "Regular";
		if (nodeDef instanceof CodeAttributeDefinition) {
			CodeAttributeDefinition codeDef = (CodeAttributeDefinition) nodeDef;
			String codeTableName = extractCodeListTableName(codeDef);
			level.table = codeTableName;
			level.column = codeTableName + rdbConfig.getIdColumnSuffix();
			level.nameColumn = codeTableName.substring(0, codeTableName.length() - rdbConfig.getCodeListTableSuffix().length()) + "_label_" + survey.getDefaultLanguage();
		} else {
			level.column = attrName;
		}
		return level;
	}

	private String extractLabel(NodeDefinition nodeDef) {
		String attrLabel = nodeDef.getLabel(NodeLabel.Type.INSTANCE);
		if (attrLabel == null) {
			attrLabel = nodeDef.getName();
		}
		return attrLabel;
	}
	

}
