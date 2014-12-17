package org.openforis.collect.io.metadata.collectearth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openforis.collect.model.CollectSurvey;
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
	
	private static final String ID_SUFFIX = "_id";
	private static final String CODE_LIST_TABLE_SUFFIX = "_code";
	private static final String CODE_LIST_ID_SUFFIX = "_code_id";
	private static final String SAIKU_SCHEMA_PLACEHOLDER = "${saikuDbSchema}";
	private CollectSurvey survey;

	public MondrianCubeGenerator(CollectSurvey survey) {
		this.survey = survey;
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
			if (nodeDef instanceof AttributeDefinition) {
				Dimension dimension = generateDimension(nodeDef);
				
				if (nodeDef instanceof KeyAttributeDefinition && ((KeyAttributeDefinition) nodeDef).isKey()) {
					Measure measure = new Measure(nodeDef.getName() + "_count");
					measure.caption = extractLabel(nodeDef) + " Count";
					measure.aggregator = "distinct count";
					measure.datatype = "Integer";
					cube.measures.add(measure);
				} else if (nodeDef instanceof NumberAttributeDefinition) {
					String[] aggregators = new String[] {"min", "max", "avg"};
					for (String aggregator : aggregators) {
						Measure measure = new Measure(nodeDef.getName());
						measure.caption = extractLabel(nodeDef);
						measure.aggregator = aggregator;
						measure.datatype = "Integer";
						cube.measures.add(measure);
					}
				} 
				cube.dimensions.add(dimension);
			} else {
				String rootEntityIdColumnName = "_" + rootEntityDef.getName() + ID_SUFFIX;
				
				String entityName = nodeDef.getName();
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
						join.leftKey = childDef.getName() + CODE_LIST_ID_SUFFIX;
						join.rightKey = codeListName + CODE_LIST_ID_SUFFIX;
						join.tables = Arrays.asList(
								new Table(entityName), 
								new Table(codeListName + CODE_LIST_TABLE_SUFFIX)
						);
						hierarchy.join = join;
					}
					hierarchy.levels.add(generateLevel(childDef));
					
					dimension.hierarchy = hierarchy;
					cube.dimensions.add(dimension);
				}
			}
		}
		
		return cube;
	}

	private Dimension generateDimension(NodeDefinition nodeDef) {
		String attrName = nodeDef.getName();
		String attrLabel = extractLabel(nodeDef);
		Dimension dimension = new Dimension(attrLabel);
		
		Hierarchy hierarchy = dimension.hierarchy;
		
		if (nodeDef instanceof CodeAttributeDefinition) {
			String codeTableName = extractCodeListTableName((CodeAttributeDefinition) nodeDef);
			dimension.foreignKey = attrName + CODE_LIST_ID_SUFFIX;
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
		return codeListName + CODE_LIST_TABLE_SUFFIX;
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
			level.column = codeTableName + ID_SUFFIX;
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
