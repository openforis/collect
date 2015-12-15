package org.openforis.collect.io.metadata.collectearth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import org.apache.commons.lang3.StringEscapeUtils;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.collect.relational.util.CodeListTables;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.EntityDefinition.TraversalType;
import org.openforis.idm.metamodel.KeyAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.NodeLabel;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition.Type;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;

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
	
	private static final String[] MEASURE_AGGREGATORS = new String[] {"min", "max", "avg"};
	
	private CollectSurvey survey;
	private RelationalSchemaConfig rdbConfig;
	private String language;
	private String dbSchemaName;
	private RelationalSchema rdbSchema;

	public MondrianCubeGenerator(CollectSurvey survey, String language, String dbSchemaName) {
		this(survey, language, dbSchemaName, RelationalSchemaConfig.createDefault());
	}
	
	public MondrianCubeGenerator(CollectSurvey survey, String language, String dbSchemaName, RelationalSchemaConfig rdbConfig) {
		this.survey = survey;
		this.language = language;
		this.rdbConfig = rdbConfig;
		this.dbSchemaName = dbSchemaName;
		
		this.rdbSchema = generateRdbSchema();
	}
	
	private Schema generateSchema() {
		final Schema schema = new Schema(survey.getName());
		List<EntityDefinition> rootEntityDefs = survey.getSchema().getRootEntityDefinitions();
		for (EntityDefinition entityDef : rootEntityDefs) {
			entityDef.traverse(new NodeDefinitionVisitor() {
				public void visit(NodeDefinition def) {
					if (def instanceof EntityDefinition && def.isMultiple()) {
						Cube cube = generateCube((EntityDefinition) def);
						schema.cubes.add(cube);
					}
				}
			});
		}
		return schema;
	}
	
	private RelationalSchema generateRdbSchema() {
		RelationalSchemaGenerator generator = new RelationalSchemaGenerator(rdbConfig);
		RelationalSchema rdbSchema = generator.generateSchema(survey, dbSchemaName);
		return rdbSchema;
	}
	
	public String generateXMLSchema() {
		MondrianCubeGenerator.Schema mondrianSchema = generateSchema();
		XStream xStream = new XStream();
		xStream.processAnnotations(MondrianCubeGenerator.Schema.class);
		String xmlSchema = xStream.toXML(mondrianSchema);
		return xmlSchema;
	}

	private Cube generateCube(EntityDefinition entityDef) {
		Cube cube = new Cube(entityDef.getLabel(NodeLabel.Type.INSTANCE, language) );
		Table table = new Table(dbSchemaName, entityDef.getName());
		cube.tables.add(table);
		
		Stack<NodeDefinition> stack = new Stack<NodeDefinition>();
		stack.addAll(entityDef.getChildDefinitions());
		while (! stack.isEmpty()) {
			NodeDefinition def = stack.pop();
			if (def instanceof AttributeDefinition && isAttributeIncluded((AttributeDefinition) def)) {
				AttributeDefinition attrDef = (AttributeDefinition) def;
				EntityDefinition parentDef = def.getParentEntityDefinition();
				if (parentDef.isRoot()) {
					addAttributeMeasuresAndDimension(cube, attrDef);
				} else {
					addNestedAttributeDimension(cube, attrDef);
				}
			} else if (def instanceof EntityDefinition && ! def.isMultiple()) {
				stack.addAll(((EntityDefinition) def).getChildDefinitions());
			}
		}
		if (survey.getTarget() == SurveyTarget.COLLECT_EARTH) {
			cube.measures.addAll(1, generateEarthSpecificMeasures());
		}
		return cube;
	}

	private Cube generateCube() {
		final EntityDefinition rootEntityDef = survey.getSchema().getRootEntityDefinitions().get(0);
		final Cube cube = new Cube("Collect Data - " + rootEntityDef.getLabel(NodeLabel.Type.INSTANCE, language) );
		
		Table table = new Table(dbSchemaName, rootEntityDef.getName());
		cube.tables.add(table);
		
		rootEntityDef.traverse(new NodeDefinitionVisitor() {
			public void visit(NodeDefinition nodeDef) {
//				if (nodeDef instanceof EntityDefinition && (((EntityDefinition) nodeDef).isRoot() || nodeDef.isMultiple())) {
//					Table table = new Table(dbSchemaName, nodeDef.getName());
//					cube.tables.add(table);
//				}
				EntityDefinition parentDef = nodeDef.getParentEntityDefinition();
				if (nodeDef instanceof AttributeDefinition) {
					AttributeDefinition attrDef = (AttributeDefinition) nodeDef;
					if (parentDef.isRoot()) {
						addAttributeMeasuresAndDimension(cube, attrDef);
					} else {
						addNestedAttributeDimension(cube, attrDef);
					}
				}
			}
		}, TraversalType.BFS);

		//add predefined dimensions
		// DEPRECATED 07/08/2015 : From now on all the operations to calculate the aspect,elevation,slope and initial land use class are made through Calculated Members
//		cube.dimensions.addAll(generatePredefinedDimensions());
		//add predefined measures
		
		// Add the measures AFTER the 1st measure, which should be Plot Count
		// Only for Collect Earth surveys
		if (survey.getTarget() == SurveyTarget.COLLECT_EARTH) {
			cube.measures.addAll(1, generateEarthSpecificMeasures());
		}
		return cube;
	}

	private void addAttributeMeasuresAndDimension(Cube cube, AttributeDefinition attrDef) {
		EntityDefinition rootEntityDef = attrDef.getRootEntity();
		String attrName = attrDef.getName();
		
		if (attrDef instanceof KeyAttributeDefinition && ((KeyAttributeDefinition) attrDef).isKey()) {
			Measure measure = new Measure(rootEntityDef.getName() + "_count");
			if (survey.getTarget() == SurveyTarget.COLLECT_EARTH) {
				measure.column = "_" + rootEntityDef.getName() + "_" + attrName;
			} else {
				measure.column = attrName;
			}
			measure.caption = StringEscapeUtils.escapeHtml4( extractLabel(rootEntityDef) + " Count" );
			measure.aggregator = "distinct count";
			measure.datatype = "Integer";
			cube.measures.add(measure);
		} else if (attrDef instanceof NumberAttributeDefinition) {
			for (String aggregator : MEASURE_AGGREGATORS) {
				Measure measure = new Measure(attrName + "_" + aggregator);
				measure.column = attrName;
				measure.caption = StringEscapeUtils.escapeHtml4( extractLabel(attrDef) + " " + aggregator );
				measure.aggregator = aggregator;
				measure.datatype = "Numeric";
				measure.formatString = "#.##";
				cube.measures.add(measure);
			}
		} 
		Dimension dimension = generateDimension(attrDef);
		cube.dimensions.add(dimension);
	}
	
	private void addNestedAttributeDimension(Cube cube, AttributeDefinition attrDef) {
		EntityDefinition rootEntityDef = attrDef.getRootEntity();
		EntityDefinition parentDef = attrDef.getParentEntityDefinition();
		String rootEntityIdColumnName = getRootEntityIdColumnName(rootEntityDef);
		
		String parentEntityName = parentDef.getName();
		String parentEntityLabel = extractLabel(parentDef);
		
		String nodeLabel = parentEntityLabel + " - " + extractLabel(attrDef);
		Dimension dimension = new Dimension(nodeLabel);
		
		if( attrDef.isMultiple() ){
			Hierarchy hierarchy = new Hierarchy(nodeLabel);
			dimension.foreignKey = rootEntityIdColumnName;
			
			if (attrDef instanceof CodeAttributeDefinition) {
				CodeAttributeDefinition codeAttrDef = (CodeAttributeDefinition) attrDef;
				CodeList codeList = codeAttrDef.getList();
				if (! codeList.isExternal()) {
					hierarchy.primaryKey = rootEntityIdColumnName;
					hierarchy.primaryKeyTable = parentEntityName;
					Join join = new Join(null);
					join.leftKey =  attrDef.getName() + rdbConfig.getCodeListTableSuffix() + rdbConfig.getIdColumnSuffix();
					String codeTableName = CodeListTables.getTableName(rdbConfig, codeList, codeAttrDef.getListLevelIndex());
					join.rightKey = CodeListTables.getIdColumnName(rdbConfig, codeTableName);
					
					join.tables = Arrays.asList(
							new Table(dbSchemaName, parentEntityName), 
							new Table(dbSchemaName, codeTableName)
					);
					hierarchy.join = join;
				}
			} else {
				hierarchy.primaryKey = rootEntityIdColumnName;
				hierarchy.primaryKeyTable = parentEntityName;
				hierarchy.table = new Table(dbSchemaName, parentEntityName);
			}
			hierarchy.levels.add(generateLevel(attrDef));
			dimension.hierarchy = hierarchy;
		} else {
			dimension = generateDimension(attrDef);
		}
		cube.dimensions.add(dimension);
	}
	
	private String getRootEntityIdColumnName(EntityDefinition rootEntityDef) {
		return rdbConfig.getIdColumnPrefix() + rootEntityDef.getName() + rdbConfig.getIdColumnSuffix();
	}
	
	private List<Measure> generateEarthSpecificMeasures() {
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
	private Dimension generateDimension(AttributeDefinition attrDef) {
		String attrName = attrDef.getName();
		String attrLabel = extractLabel(attrDef);
		Dimension dimension = new Dimension(String.format("%s [%s]", attrLabel, attrDef.getName()));
		
		Hierarchy hierarchy = dimension.hierarchy;
		DataTable dataTable = rdbSchema.getDataTable(attrDef.getParentEntityDefinition());
		hierarchy.table = new Table(dbSchemaName, dataTable.getName());
		
		if (attrDef instanceof CodeAttributeDefinition) {
			CodeAttributeDefinition codeAttrDef = (CodeAttributeDefinition) attrDef;
			
			if (! codeAttrDef.getList().isExternal()) {
				EntityDefinition rootEntityDef = attrDef.getRootEntity();
				
				String entityName = attrName;
				
				if( attrDef.isMultiple() ){
					String rootEntityIdColumnName = getRootEntityIdColumnName(rootEntityDef);
					dimension.foreignKey = rootEntityIdColumnName;
					hierarchy.primaryKey = rootEntityIdColumnName;
					hierarchy.primaryKeyTable = entityName;
						
					Join join = new Join(null);
					String codeListTableName = CodeListTables.getTableName(rdbConfig, codeAttrDef);
	
					join.leftKey = CodeListTables.getIdColumnName(rdbConfig, codeListTableName);
					join.rightKey = CodeListTables.getIdColumnName(rdbConfig, codeListTableName);
					
					join.tables = Arrays.asList(
							new Table(dbSchemaName, entityName), 
							new Table(dbSchemaName, codeListTableName)
					);
					
					hierarchy.join = join;
				} else {			
					String codeListTableName = CodeListTables.getTableName(rdbConfig, codeAttrDef);
					dimension.foreignKey = attrName + rdbConfig.getCodeListTableSuffix() + rdbConfig.getIdColumnSuffix();						
					hierarchy.table = new Table(dbSchemaName, codeListTableName);
				}
			}
			hierarchy.levels.add(generateLevel(attrDef));
		} else if (attrDef instanceof DateAttributeDefinition) {
			dimension.type = "";
			hierarchy.type = "TimeDimension";
			hierarchy.allMemberName = "attrLabel";
			String[] levelNames = new String[] {"Year", "Month", "Day"};
			for (String levelName : levelNames) {
				Level level = new Level(attrLabel + " - " + levelName);
				level.column = attrDef.getName() + "_" + levelName.toLowerCase(Locale.ENGLISH);
				level.levelType = String.format("Time%ss", levelName);
				level.type = "Numeric";
				hierarchy.levels.add(level);
			}
		} else if (attrDef instanceof CoordinateAttributeDefinition) {
			dimension.type = "";
			hierarchy.type = "StandardDimension";

			Level level = new Level(attrLabel + " - Latitude");
			level.column = attrDef.getName() + "_y";
			hierarchy.levels.add(level);
			
			Level level2 = new Level(attrLabel + " - Longitude");
			level2.column = attrDef.getName() + "_x";
			hierarchy.levels.add(level2);
		} else {
			hierarchy.levels.add(generateLevel(attrDef));
		}
		return dimension;
	}

	private Level generateLevel(NodeDefinition nodeDef) {
		String attrName = nodeDef.getName();
		String attrLabel = extractLabel(nodeDef);
		Level level = new Level(attrLabel);
		level.levelType = "Regular";
		if (nodeDef instanceof NumericAttributeDefinition) {
			level.type = ((NumericAttributeDefinition) nodeDef).getType() == Type.INTEGER ? "Integer": "Numeric";
		} else {
			level.type = "String";	
		}
		if (nodeDef instanceof CodeAttributeDefinition && ! ((CodeAttributeDefinition) nodeDef).getList().isExternal()) {
			CodeAttributeDefinition codeDef = (CodeAttributeDefinition) nodeDef;
			String codeTableName = CodeListTables.getTableName(rdbConfig, codeDef);
			level.table = codeTableName;
			level.column = codeTableName + rdbConfig.getIdColumnSuffix();
			level.nameColumn = codeTableName.substring(0, codeTableName.length() - rdbConfig.getCodeListTableSuffix().length()) + "_label_" + language ;
		} else {
			level.column = attrName;
		}
		return level;
	}

	private String extractLabel(NodeDefinition nodeDef) {
		String attrLabel = nodeDef.getLabel(NodeLabel.Type.INSTANCE, language);
		if (attrLabel == null) {
			attrLabel = nodeDef.getName();
		}
		return attrLabel;
	}
	
	private boolean isAttributeIncluded(AttributeDefinition def) {
		return def instanceof CodeAttributeDefinition
				|| def instanceof DateAttributeDefinition
				|| def instanceof NumberAttributeDefinition
				|| def instanceof TextAttributeDefinition
				|| def instanceof TimeAttributeDefinition
				;
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
		
		@XStreamImplicit
		private List<Cube> cubes = new ArrayList<Cube>();

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
		
		@XStreamImplicit
		private List<Table> tables = new ArrayList<Table>();
		
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
