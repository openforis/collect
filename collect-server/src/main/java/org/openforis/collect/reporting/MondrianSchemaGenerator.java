package org.openforis.collect.reporting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringEscapeUtils;
import org.openforis.collect.earth.core.rdb.RelationalSchemaContext;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

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

	public MondrianSchemaGenerator(CollectSurvey survey) {
		this.survey = survey;
		this.rdbConfig = new RelationalSchemaContext().getRdbConfig();
	}
	
	public Schema generateSchema() {
		Schema schema = new Schema(survey.getName());
		Cube cube = generateCube();
		schema.cube = cube;
		
		return schema;
	}
	
	public String generateXMLSchema() {
		MondrianSchemaGenerator.Schema mondrianSchema = generateSchema();
		XStream xStream = new XStream();
		xStream.processAnnotations(MondrianSchemaGenerator.Schema.class);
		String xmlSchema = xStream.toXML(mondrianSchema);
		return xmlSchema;
	}

	private Cube generateCube() {
		Cube cube = new Cube(survey.getName());
		String schemaName = getDBSchemaName(survey);
		EntityDefinition rootEntityDef = survey.getSchema().getRootEntityDefinitions().get(0);
		Table table = new Table(schemaName, rootEntityDef.getName());
		cube.table = table;
		
		List<NodeDefinition> children = rootEntityDef.getChildDefinitions();
		for (NodeDefinition nodeDef : children) {
			String nodeName = nodeDef.getName();
			if (nodeDef instanceof AttributeDefinition) {
				Dimension dimension = generateDimension(nodeDef, rootEntityDef );
				
				if (nodeDef instanceof KeyAttributeDefinition && ((KeyAttributeDefinition) nodeDef).isKey()) {
					Measure measure = new Measure(rootEntityDef.getName() + "_count");
					measure.column = "_" + rootEntityDef.getName() + "_" + nodeName;
					measure.caption = StringEscapeUtils.escapeHtml4( extractLabel(rootEntityDef) + " Count" );
					measure.aggregator = "distinct count";
					measure.datatype = "Integer";
					cube.measures.add(measure);
				} else if (nodeDef instanceof NumberAttributeDefinition) {
					for (String aggregator : MEASURE_AGGREGATORS) {
						Measure measure = new Measure(nodeName + "_" + aggregator);
						measure.column = nodeName;
						measure.caption = StringEscapeUtils.escapeHtml4( extractLabel(nodeDef) + " " + aggregator );
						measure.aggregator = aggregator;
						measure.datatype = "Numeric";
						measure.formatString = "#.##";
						cube.measures.add(measure);
					}
				} 
				cube.dimensions.add(dimension);
			} else {
				String rootEntityIdColumnName = getRootEntityIdColumnName(rootEntityDef);
				
				String entityName = nodeName;
				String entityLabel = extractLabel(nodeDef);
				
				for (NodeDefinition childDef : ((EntityDefinition) nodeDef).getChildDefinitions()) {
					String childLabel = entityLabel + " - " + extractLabel(childDef);
					Dimension dimension = new Dimension(childLabel);
					Hierarchy hierarchy = new Hierarchy(childLabel);
					
					if( nodeDef.isMultiple() ){
						dimension.foreignKey = rootEntityIdColumnName;
						hierarchy.primaryKey = rootEntityIdColumnName;
						hierarchy.primaryKeyTable = entityName;
						
						if (childDef instanceof CodeAttributeDefinition) {
							
							Join join = new Join(null);
							String codeListName = ((CodeAttributeDefinition) childDef).getList().getName();
							join.leftKey = childDef.getName() + rdbConfig.getCodeListTableSuffix() + rdbConfig.getIdColumnSuffix();
							join.rightKey = codeListName + rdbConfig.getCodeListTableSuffix() + rdbConfig.getIdColumnSuffix();
							
							join.tables = Arrays.asList(
									new Table(schemaName, entityName), 
									new Table(schemaName, codeListName + rdbConfig.getCodeListTableSuffix())
							);
							
							hierarchy.join = join;
							
						}else{
							hierarchy.table = new Table(schemaName, entityName);
						}
						
						hierarchy.levels.add(generateLevel(childDef));
						
						dimension.hierarchy = hierarchy;
						
					}else{
						dimension = generateDimension(childDef, rootEntityDef);
					}
					
					cube.dimensions.add(dimension);
				}
			}
		}
		//add predefined dimensions
		// DEPRECATED 07/08/2015 : From now on all the operations to calculate the aspect,elevation,slope and initial land use class are made through Calculated Members
//		cube.dimensions.addAll(generatePredefinedDimensions());
		//add predefined measures
		cube.measures.addAll(0, generatePredefinedMeasures());
		return cube;
	}

	public String getRootEntityIdColumnName(EntityDefinition rootEntityDef) {
		return rdbConfig.getIdColumnPrefix() + rootEntityDef.getName() + rdbConfig.getIdColumnSuffix();
	}
	
	
	private List<Measure> generatePredefinedMeasures() {
		List<Measure> measures = new ArrayList<Measure>();
		//Expansion factor - Area
		{
			Measure measure = new Measure("area");
			measure.column = "expansion_factor";
			measure.caption = "Area (HA)";
			measure.aggregator = "sum";
			measure.datatype = "Integer";
			measure.formatString = "###,###";
			measures.add(measure);
		}
		
		// Plot weight
		{
			Measure measure = new Measure("plot_weight");
			measure.column = "plot_weight";
			measure.caption = "Plot Weight";
			measure.aggregator = "sum";
			measure.datatype = "Integer";
			measure.formatString = "#,###";
			measures.add(measure);
		}
		
		return measures;
	}
	
/*	private List<Dimension> generatePredefinedDimensions() {
		List<Dimension> dimensions = new ArrayList<Dimension>();
		//Slope category
		{
			Dimension d = new Dimension("Slope category");
			d.foreignKey = "slope_id";
			d.highCardinality = "false";
			Hierarchy h = new Hierarchy();
			h.table = new Table("slope_category");
			Level l = new Level("Slope_category");
			l.table = "slope_category";
			l.column = "slope_id";
			l.nameColumn = "slope_caption";
			l.type = "String";
			l.levelType = "Regular";
			l.uniqueMembers = "false";
			h.levels.add(l);
			d.hierarchy = h;
			dimensions.add(d);
		}
		//Initial Land Use
		{
			Dimension d = new Dimension("Initial Land Use");
			d.foreignKey = "dynamics_id";
			d.highCardinality = "false";
			Hierarchy h = new Hierarchy();
			h.table = new Table("dynamics_category");
			Level l = new Level("Initial_land_use");
			l.table = "dynamics_category";
			l.column = "dynamics_id";
			l.nameColumn = "dynamics_caption";
			l.type = "String";
			l.levelType = "Regular";
			l.uniqueMembers = "false";
			l.hideMemberIf = "Never";
			h.levels.add(l);
			d.hierarchy = h;
			dimensions.add(d);
		}
		//Elevation Range
		{
			Dimension d = new Dimension("Elevation range");
			d.foreignKey = "elevation_id";
			d.highCardinality = "false";
			Hierarchy h = new Hierarchy();
			h.table = new Table("elevation_category");
			Level l = new Level("Elevation_range");
			l.table = "elevation_category";
			l.column = "elevation_id";
			l.nameColumn = "elevation_caption";
			l.type = "String";
			l.levelType = "Regular";
			l.uniqueMembers = "false";
			l.hideMemberIf = "Never";
			h.levels.add(l);
			d.hierarchy = h;
			dimensions.add(d);
		}
		return dimensions;
	}
*/
	private Dimension generateDimension(NodeDefinition nodeDef, EntityDefinition rootEntityDef ) {
		String schemaName = getDBSchemaName(nodeDef);
		String attrName = nodeDef.getName();
		String attrLabel = extractLabel(nodeDef);
		Dimension dimension = new Dimension(attrLabel);
		
		Hierarchy hierarchy = dimension.hierarchy;
		
		if (nodeDef instanceof CodeAttributeDefinition) {
			String rootEntityIdColumnName = getRootEntityIdColumnName(rootEntityDef);
			
			String entityName = attrName;
			
			if( nodeDef.isMultiple() ){
				
				dimension.foreignKey = rootEntityIdColumnName;
				hierarchy.primaryKey = rootEntityIdColumnName;
				hierarchy.primaryKeyTable = entityName;
					
				Join join = new Join(null);
				String codeListName = ((CodeAttributeDefinition) nodeDef).getList().getName();
				join.leftKey = codeListName + rdbConfig.getCodeListTableSuffix() + rdbConfig.getIdColumnSuffix();
				join.rightKey = codeListName + rdbConfig.getCodeListTableSuffix() + rdbConfig.getIdColumnSuffix();
				
				join.tables = Arrays.asList(
						new Table(schemaName, entityName), 
						new Table(schemaName, codeListName + rdbConfig.getCodeListTableSuffix())
				);
				
				hierarchy.join = join;
										
			} else {			
				CodeAttributeDefinition codeAttrDef = (CodeAttributeDefinition) nodeDef;
				String codeTableName = extractCodeListTableName(codeAttrDef);
				dimension.foreignKey = attrName + rdbConfig.getCodeListTableSuffix() + rdbConfig.getIdColumnSuffix();						
				hierarchy.table = new Table(schemaName, codeTableName);
			}
		}
		
		if (nodeDef instanceof DateAttributeDefinition) {
			dimension.type = "";
			hierarchy.type = "TimeDimension";
			hierarchy.allMemberName = "attrLabel";
			String[] levelNames = new String[] {"Year", "Month", "Day"};
			for (String levelName : levelNames) {
				Level level = new Level(attrLabel + " - " + levelName);
				level.column = nodeDef.getName() + "_" + levelName.toLowerCase(Locale.ENGLISH);
				level.levelType = String.format("Time%ss", levelName);
				level.type = "Numeric";
				hierarchy.levels.add(level);
			}
		} else {
			Level level = generateLevel(nodeDef);
			hierarchy.levels.add(level);
		}
		return dimension;
	}

	private String getDBSchemaName(NodeDefinition nodeDef) {
		return getDBSchemaName((CollectSurvey) nodeDef.getSurvey());
	}

	private String getDBSchemaName(CollectSurvey survey) {
		return survey.getName();
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
		Level level = new Level(attrLabel);
		if (nodeDef instanceof NumericAttributeDefinition) {
			level.type = ((NumericAttributeDefinition) nodeDef).getType() == Type.INTEGER ? "Integer": "Numeric";
		} else if (nodeDef instanceof BooleanAttributeDefinition) {
			level.type = "Boolean";
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
	

	private static class MondrianSchemaObject {
		
		@XStreamAsAttribute
		private String name;

		public MondrianSchemaObject(String name) {
			super();
			this.name = StringEscapeUtils.escapeHtml4(name);
		}
		
	}
	
	@XStreamAlias("Schema")
	private static class Schema extends MondrianSchemaObject {
		
		@XStreamAlias("Cube")
		private Cube cube;

		public Schema(String name) {
			super(name);
		}
		
	}
	
	@XStreamAlias("Cube")
	private static class Cube extends MondrianSchemaObject {
		
		@XStreamAsAttribute
		private String cache = "true";
		
		@XStreamAsAttribute
		private String enabled = "true";
		
		@XStreamAlias("Table")
		private Table table;
		
		@XStreamImplicit
		private List<Dimension> dimensions = new ArrayList<Dimension>();
		
		@XStreamImplicit
		private List<Measure> measures = new ArrayList<Measure>();

		public Cube(String name) {
			super(name);
		}
	}
	
	@XStreamAlias("Table")
	private static class Table extends MondrianSchemaObject {

		@XStreamAsAttribute
		private final String schema;
		
		public Table(String schema, String name) {
			super(name);
			this.schema = schema;
		}
		
	}
	
	@XStreamAlias("Dimension")
	private static class Dimension extends MondrianSchemaObject {
		
		@XStreamAsAttribute
		private String foreignKey;
		
		@XStreamAsAttribute
		public String type = "StandardDimension";

		@XStreamAsAttribute
		public String visible = "true";

		@XStreamAsAttribute
		public String highCardinality;

		@XStreamAlias("Hierarchy")
		private Hierarchy hierarchy = new Hierarchy(null);
		
		public Dimension(String name) {
			super(name);
		}
		
	}
	
	@XStreamAlias("Hierarchy")
	private static class Hierarchy extends MondrianSchemaObject {
		
		@XStreamAsAttribute
		public String type;
		
		@XStreamAsAttribute
		public String allMemberName;

		@XStreamAsAttribute
		private String primaryKey;
		
		@XStreamAsAttribute
		private String primaryKeyTable;
		
		@XStreamAlias("Table")
		private Table table;
		
		@XStreamAlias("Join")
		private Join join;
		
		@XStreamAsAttribute
		private String visible = "true";
		
		@XStreamAsAttribute
		private String hasAll = "true";
		
		@XStreamImplicit
		private List<Level> levels = new ArrayList<Level>();
		
		public Hierarchy(String name) {
			super(name);
		}
		
	}
	
	@XStreamAlias("Level")
	private static class Level extends MondrianSchemaObject {
		
		@XStreamAsAttribute
		private String table;
		
		@XStreamAsAttribute
		private String column;
		
		@XStreamAsAttribute
		private String nameColumn;
		
		@XStreamAsAttribute
		private String uniqueMembers ="true";
		
		@XStreamAsAttribute
		private String levelType;
		
		@XStreamAsAttribute
		private String type;
		
		@XStreamAsAttribute
		public String hideMemberIf;

		public Level(String name) {
			super(name);
		}
	}
	
	@XStreamAlias("Measure")
	private static class Measure extends MondrianSchemaObject {

		@XStreamAsAttribute
		private String column;
		
		@XStreamAsAttribute
		private String datatype;
		
		@XStreamAsAttribute
		private String aggregator;
		
		@XStreamAsAttribute
		private String caption;
		
		@XStreamAsAttribute
		private String formatString;
		
		
		public Measure(String name) {
			super(name);
		}
		
	}
	
	@XStreamAlias("Join")
	private static class Join extends MondrianSchemaObject {
		
		@XStreamAsAttribute
		private String leftKey;
		
		@XStreamAsAttribute
		private String rightKey;
		
		@XStreamImplicit
		private List<Table> tables;
		
		public Join(String name) {
			super(name);
		}

	}
}
