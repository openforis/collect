package org.openforis.collect.io.metadata.collectearth;

import static org.openforis.collect.io.metadata.collectearth.balloon.HtmlUnicodeEscaperUtil.escapeMondrianUnicode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringEscapeUtils;
import org.openforis.collect.earth.core.rdb.RelationalSchemaContext;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.model.CodeTable;
import org.openforis.collect.relational.model.CodeValueFKColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.collect.relational.util.CodeListTables;
import org.openforis.collect.relational.util.DataTables;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
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
public class MondrianCubeGenerator {
	
	private static final String SAIKU_SCHEMA_PLACEHOLDER = "${saikuDbSchema}";
	private static final String[] MEASURE_AGGREGATORS = new String[] {"min", "max", "avg"};
	private static final String BOOLEAN_DATATYPE = "Boolean";
	private static final String INTEGER_DATATYPE = "Integer";
	private static final String NUMERIC_DATATYPE = "Numeric";
	private static final String STRING_DATATYPE = "String";
	
	private CollectSurvey survey;
	private RelationalSchemaConfig rdbConfig;
	private String language;
	private RelationalSchema rdbSchema;

	public MondrianCubeGenerator(CollectSurvey survey, String language) {
		this.survey = survey;
		this.language = language;
		this.rdbConfig = new RelationalSchemaContext().getRdbConfig();
	}
	
	public Schema generateSchema() {
		this.rdbSchema = new RelationalSchemaGenerator(rdbConfig).generateSchema(survey, survey.getName());
		Cube cube = generateCube();
		Schema schema = new Schema(survey.getName());
		schema.cube = cube;
		return schema;
	}
	
	public String generateXMLSchema() {
		MondrianCubeGenerator.Schema mondrianSchema = generateSchema();
		XStream xStream = new XStream();
		xStream.processAnnotations(MondrianCubeGenerator.Schema.class);
		String xmlSchema = xStream.toXML(mondrianSchema);
		return xmlSchema;
	}

	private Cube generateCube() {
		Cube cube = new Cube("Collect Earth Plot");
		EntityDefinition rootEntityDef = survey.getSchema().getFirstRootEntityDefinition();
		Table table = new Table(rootEntityDef.getName());
		cube.table = table;
		
		List<NodeDefinition> children = rootEntityDef.getChildDefinitions();
		for (NodeDefinition nodeDef : children) {
			if (! survey.getAnnotations().isIncludedInDataExport(nodeDef)) {
				continue;
			}
			String nodeName = nodeDef.getName();
			if (nodeDef instanceof AttributeDefinition) {
				Dimension dimension = generateDimension(nodeDef, rootEntityDef );
				
				if (nodeDef instanceof KeyAttributeDefinition && ((KeyAttributeDefinition) nodeDef).isKey()) {
					Measure measure = new Measure(rootEntityDef.getName() + "_count");
					measure.column = "_" + rootEntityDef.getName() + "_" + nodeName;
					measure.caption = StringEscapeUtils.escapeHtml4( extractFailsafeLabel(rootEntityDef) + " Count" );
					measure.aggregator = "distinct count";
					measure.datatype = "Integer";
					cube.measures.add(measure);
				} else if (nodeDef instanceof NumberAttributeDefinition) {
					for (String aggregator : MEASURE_AGGREGATORS) {
						Measure measure = new Measure(nodeName + "_" + aggregator);
						measure.column = nodeName;
						measure.caption = StringEscapeUtils.escapeHtml4( extractFailsafeLabel(nodeDef) + " " + aggregator );
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
				String entityLabel = extractFailsafeLabel(nodeDef);
				
				for (NodeDefinition childDef : ((EntityDefinition) nodeDef).getChildDefinitions()) {
					String childLabel = extractReportingLabel(childDef);
					if (childLabel == null) {
						childLabel = extractFailsafeLabel(childDef);
						if (! childLabel.startsWith(entityLabel)) {
							childLabel = entityLabel + " " + childLabel;
						}
					}
					Dimension dimension = new Dimension(childLabel);
					Hierarchy hierarchy = new Hierarchy(childLabel);
					
					if( nodeDef.isMultiple() ){
						dimension.foreignKey = rootEntityIdColumnName;
						hierarchy.primaryKey = rootEntityIdColumnName;
						hierarchy.primaryKeyTable = entityName;
						
						if (childDef instanceof CodeAttributeDefinition) {
							CodeAttributeDefinition codeAttr = (CodeAttributeDefinition) childDef;
							
							Join join = new Join(null);
							
							DataTable dataTable = rdbSchema.getDataTable(nodeDef);
							CodeValueFKColumn foreignKeyCodeColumn = dataTable.getForeignKeyCodeColumn(codeAttr);
							join.leftKey = foreignKeyCodeColumn.getName();
							
							CodeTable codeListTable = rdbSchema.getCodeListTable(codeAttr);
							join.rightKey = CodeListTables.getIdColumnName(rdbConfig, codeListTable.getName());;
							
							join.tables = Arrays.asList(
									new Table(entityName), 
									new Table(codeListTable.getName())
							);
							hierarchy.join = join;
						}else{
							hierarchy.table = new Table(entityName);
						}
						
						hierarchy.levels.addAll(generateLevel(childDef));
						
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
		
		// Add the measures AFTER the 1st measure, which shouyld be Plot Count
		cube.measures.addAll(1, generatePredefinedMeasures());
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
			measure.datatype = "Numeric";
			measure.formatString = "###,###.00";
			measures.add(measure);
		}
		
		// Plot weight
		{
			Measure measure = new Measure("plot_weight");
			measure.column = "plot_weight";
			measure.caption = "Plot Weight";
			measure.aggregator = "sum";
			measure.datatype = "Numeric";
			measure.formatString = "#,###.##";
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
		String attrLabel = extractFailsafeLabel(nodeDef);
		Dimension dimension = new Dimension(attrLabel);
		
		Hierarchy hierarchy = dimension.hierarchy;
		
		if (nodeDef instanceof CodeAttributeDefinition) {
			CodeAttributeDefinition codeDef = (CodeAttributeDefinition) nodeDef;
			DataTable dataTable = nodeDef.isMultiple() ? rdbSchema.getDataTable(nodeDef) : rdbSchema.getDataTable(nodeDef.getParentDefinition());
			CodeTable codeListTable = rdbSchema.getCodeListTable(codeDef);
			String codeListTableName = codeListTable.getName();
			String codeFKColumnName = DataTables.getCodeFKColumnName(rdbConfig, dataTable, codeDef);

			String rootEntityIdColumnName = getRootEntityIdColumnName(rootEntityDef);
			
			if( nodeDef.isMultiple() ){
				dimension.foreignKey = rootEntityIdColumnName;
				hierarchy.primaryKey = rootEntityIdColumnName;
				hierarchy.primaryKeyTable = dataTable.getName();
					
				Join join = new Join(null);
				
				join.leftKey = codeFKColumnName;
				join.rightKey = CodeListTables.getIdColumnName(rdbConfig, codeListTableName);
				
				join.tables = Arrays.asList(
						new Table(dataTable.getName()), 
						new Table(codeListTableName)
				);
				hierarchy.join = join;
			} else {
				dimension.foreignKey = codeFKColumnName;
				hierarchy.table = new Table(codeListTableName);
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
				level.uniqueMembers = "false"; // Avoids grouping the values of the sane day over multiple months!!!
				hierarchy.levels.add(level);
			}
		} else if (nodeDef instanceof CoordinateAttributeDefinition) {
			dimension.type = "";
			hierarchy.type = "StandardDimension";

			Level level = new Level(attrLabel + " - Latitude");
			level.column = nodeDef.getName() + "_y";
			hierarchy.levels.add(level);
			
			Level level2 = new Level(attrLabel + " - Longitude");
			level2.column = nodeDef.getName() + "_x";
			hierarchy.levels.add(level2);
		
		} else {
			List<Level> levels = generateLevel(nodeDef);
			hierarchy.levels.addAll(levels);
		}
		return dimension;
	}

	private String extractCodeListName(CodeAttributeDefinition codeAttrDef) {
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
		return codeListName.toString();
	}
	
	private String extractCodeListTableName(CodeAttributeDefinition codeAttrDef) {
		return extractCodeListName(codeAttrDef) + rdbConfig.getCodeListTableSuffix();
	}
	
	private List<Level> generateLevel(NodeDefinition nodeDef) {
		List<Level> levels = new ArrayList<Level>();
		String attrName = nodeDef.getName();
		String attrLabel = extractFailsafeLabel(nodeDef);
		Level level = new Level(attrLabel);
		levels.add(level);
		if (nodeDef instanceof NumericAttributeDefinition) {
			level.type = ((NumericAttributeDefinition) nodeDef).getType() == Type.INTEGER ? INTEGER_DATATYPE: NUMERIC_DATATYPE;
		} else if (nodeDef instanceof CodeAttributeDefinition) {
			level.type = ((CodeAttributeDefinition) nodeDef).getList().isExternal() ? STRING_DATATYPE : INTEGER_DATATYPE;
		} else {
			level.type = STRING_DATATYPE;	
		}
		level.levelType = "Regular";
		if (nodeDef instanceof CodeAttributeDefinition) {
			CodeAttributeDefinition codeDef = (CodeAttributeDefinition) nodeDef;
			String codeTableName = extractCodeListTableName(codeDef);
			level.table = codeTableName;
			level.column = codeTableName + rdbConfig.getIdColumnSuffix();
			level.nameColumn = codeTableName.substring(0, codeTableName.length() - rdbConfig.getCodeListTableSuffix().length()) + "_label_" + language ;
			
			Level levelId = new Level( attrLabel + " -  ID");
			levelId.type = "String";	
			levelId.table = codeTableName;
			levelId.column = codeTableName + rdbConfig.getIdColumnSuffix();
			levelId.nameColumn =  extractCodeListName(codeDef);
			
			levels.add(levelId);
			
		} else {
			level.column = attrName;
		}
		
		
		
		return levels;
	}

	private String extractFailsafeLabel(NodeDefinition nodeDef) {
		String label = extractReportingLabel(nodeDef);
		if (label == null) {
			label = nodeDef.getLabel(NodeLabel.Type.INSTANCE, language);
			if (label == null) {
				label = nodeDef.getName();
			}
		}
		return escapeMondrianUnicode(label); 
	}
	
	private String extractReportingLabel(NodeDefinition nodeDef) {
		return nodeDef.getLabel(NodeLabel.Type.REPORTING, language);
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
		private final String schema = SAIKU_SCHEMA_PLACEHOLDER;
		
		public Table(String name) {
			super(name);
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
		private String uniqueMembers ="false"; // Only the plot id should be unique
		
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
