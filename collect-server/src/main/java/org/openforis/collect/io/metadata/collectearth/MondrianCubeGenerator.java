package org.openforis.collect.io.metadata.collectearth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openforis.collect.earth.core.rdb.RelationalSchemaContext;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
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
	
	private CollectSurvey survey;
	private RelationalSchemaConfig rdbConfig;

	public MondrianCubeGenerator(CollectSurvey survey) {
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
		org.openforis.collect.io.metadata.collectearth.MondrianCubeGenerator.Schema mondrianSchema = generateSchema();
		XStream xStream = new XStream();
		xStream.processAnnotations(MondrianCubeGenerator.Schema.class);
		String xmlSchema = xStream.toXML(mondrianSchema);
		return xmlSchema;
	}

	private Cube generateCube() {
		Cube cube = new Cube("Collect Earth Plot");
		EntityDefinition rootEntityDef = survey.getSchema().getRootEntityDefinitions().get(0);
		Table table = new Table(rootEntityDef.getName());
		cube.table = table;
		
		List<NodeDefinition> children = rootEntityDef.getChildDefinitions();
		for (NodeDefinition nodeDef : children) {
			String nodeName = nodeDef.getName();
			if (nodeDef instanceof AttributeDefinition) {
				Dimension dimension = generateDimension(nodeDef);
				
				if (nodeDef instanceof KeyAttributeDefinition && ((KeyAttributeDefinition) nodeDef).isKey()) {
					Measure measure = new Measure(rootEntityDef.getName() + "_count");
					measure.column = "_" + rootEntityDef.getName() + "_" + nodeName;
					measure.caption = extractLabel(rootEntityDef) + " Count";
					measure.aggregator = "distinct count";
					measure.datatype = "Integer";
					cube.measures.add(measure);
				} else if (nodeDef instanceof NumberAttributeDefinition) {
					for (String aggregator : MEASURE_AGGREGATORS) {
						Measure measure = new Measure(nodeName + "_" + aggregator);
						measure.column = nodeName;
						measure.caption = extractLabel(nodeDef) + " " + aggregator;
						measure.aggregator = aggregator;
						measure.datatype = "Integer";
						cube.measures.add(measure);
					}
				} 
				cube.dimensions.add(dimension);
			} else {
				String rootEntityIdColumnName = rdbConfig.getIdColumnPrefix() + rootEntityDef.getName() + rdbConfig.getIdColumnSuffix();
				
				String entityName = nodeName;
				String entityLabel = extractLabel(nodeDef);
				
				for (NodeDefinition childDef : ((EntityDefinition) nodeDef).getChildDefinitions()) {
					String childLabel = entityLabel + " - " + extractLabel(childDef);
					Dimension dimension = new Dimension(childLabel);
					dimension.foreignKey = rootEntityIdColumnName;
					
					Hierarchy hierarchy = new Hierarchy(childLabel);
					hierarchy.primaryKey = rootEntityIdColumnName;
					hierarchy.primaryKeyTable = entityName;
					
					if (childDef instanceof CodeAttributeDefinition) {
						Join join = new Join(null);
						String codeListName = ((CodeAttributeDefinition) childDef).getList().getName();
						join.leftKey = childDef.getName() + rdbConfig.getCodeListTableSuffix() + rdbConfig.getIdColumnSuffix();
						join.rightKey = codeListName + rdbConfig.getCodeListTableSuffix() + rdbConfig.getIdColumnSuffix();
						join.tables = Arrays.asList(
								new Table(entityName), 
								new Table(codeListName + rdbConfig.getCodeListTableSuffix())
						);
						hierarchy.join = join;
					}
					hierarchy.levels.add(generateLevel(childDef));
					
					dimension.hierarchy = hierarchy;
					cube.dimensions.add(dimension);
				}
			}
		}
		//add predefined dimensions
		cube.dimensions.addAll(generatePredefinedDimensions());
		return cube;
	}

	private List<Dimension> generatePredefinedDimensions() {
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
		//Initial Land Use
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

	private Dimension generateDimension(NodeDefinition nodeDef) {
		String attrName = nodeDef.getName();
		String attrLabel = extractLabel(nodeDef);
		Dimension dimension = new Dimension(attrLabel);
		
		Hierarchy hierarchy = dimension.hierarchy;
		
		if (nodeDef instanceof CodeAttributeDefinition) {
			String codeTableName = extractCodeListTableName((CodeAttributeDefinition) nodeDef);
			dimension.foreignKey = attrName + rdbConfig.getCodeListTableSuffix() + rdbConfig.getIdColumnSuffix();
			hierarchy.table = new Table(codeTableName);
		}
		
		if (nodeDef instanceof DateAttributeDefinition) {
			hierarchy.type = "TimeDimension";
			String[] levelNames = new String[] {"Year", "Month", "Day"};
			for (String levelName : levelNames) {
				Level level = new Level(attrLabel + " - " + levelName);
				level.column = nodeDef.getName() + "_" + levelName.toLowerCase();
				level.levelType = String.format("Time%ss", levelName);
				hierarchy.levels.add(level);
			}
		} else {
			Level level = generateLevel(nodeDef);
			hierarchy.levels.add(level);
		}
		return dimension;
	}

	private String extractCodeListTableName(CodeAttributeDefinition nodeDef) {
		String codeListName = nodeDef.getList().getName();
		return codeListName + rdbConfig.getCodeListTableSuffix();
	}
	
	private Level generateLevel(NodeDefinition nodeDef) {
		String attrName = nodeDef.getName();
		String attrLabel = extractLabel(nodeDef);
		Level level = new Level(attrLabel);
		if (nodeDef instanceof NumericAttributeDefinition) {
			level.type = ((NumericAttributeDefinition) nodeDef).getType() == Type.INTEGER ? "Integer": "Numeric";
		} else {
			level.type = "String";	
		}
		level.levelType = "Regular";
		if (nodeDef instanceof CodeAttributeDefinition) {
			String codeTableName = extractCodeListTableName((CodeAttributeDefinition) nodeDef);
			level.table = codeTableName;
			level.column = codeTableName + rdbConfig.getIdColumnSuffix();
			level.nameColumn = codeTableName + "_label_" + survey.getDefaultLanguage();
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
			this.name = name;
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
		
		public Hierarchy() {
			this(null);
		}
		
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
