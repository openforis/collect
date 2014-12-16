package org.openforis.collect.io.metadata;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.KeyAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition.Type;

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

	private Cube generateCube() {
		Cube cube = new Cube("Collect Earth Plot");
		EntityDefinition rootEntityDef = survey.getSchema().getRootEntityDefinitions().get(0);
		Table table = new Table(rootEntityDef.getName());
		//TODO
		//table.schema="${saikuDbSchema}" 
		cube.table = table;
		
		List<NodeDefinition> children = rootEntityDef.getChildDefinitions();
		for (NodeDefinition nodeDef : children) {
			if (nodeDef instanceof AttributeDefinition) {
				Dimension dimension = generateDimension(nodeDef);
				cube.dimensions.add(dimension);
				
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
				
			}
		}
		
		return cube;
	}

	private Dimension generateDimension(NodeDefinition nodeDef) {
		String attrName = nodeDef.getName();
		String attrLabel = extractLabel(nodeDef);
		Dimension dimension = new Dimension(attrLabel);
		Level level = new Level(attrLabel);
		if (nodeDef instanceof NumericAttributeDefinition) {
			level.type = ((NumericAttributeDefinition) nodeDef).getType() == Type.INTEGER ? "Integer": "Numeric";
		} else {
			level.type = "String";	
		}
		level.levelType = "Regular";
		
		if (nodeDef instanceof CodeAttributeDefinition) {
			String codeListName = ((CodeAttributeDefinition) nodeDef).getList().getName();
			String codeTableName = codeListName + "_code";
			dimension.foreignKey = attrName + "_code_id";
			dimension.hierarchy.table = new Table(codeTableName);
			level.table = codeTableName;
			level.column = codeTableName + "_id";
			level.nameColumn = codeTableName + "_label_" + survey.getDefaultLanguage();
		} else {
			level.column = attrName;
		}
		dimension.hierarchy.levels.add(level);
		return dimension;
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
	public static class Schema extends MondrianSchemaObject {
		
		@XStreamAlias("Cube")
		private Cube cube;

		public Schema(String name) {
			super(name);
		}
		
	}
	
	@XStreamAlias("Cube")
	public static class Cube extends MondrianSchemaObject {
		
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
	public static class Table extends MondrianSchemaObject {

		public Table(String name) {
			super(name);
		}
		
	}
	
	@XStreamAlias("Dimension")
	public static class Dimension extends MondrianSchemaObject {
		
		@XStreamAsAttribute
		private String foreignKey;
		
		@XStreamAlias("Hierarchy")
		private Hierarchy hierarchy = new Hierarchy(null);
		
		public Dimension(String name) {
			super(name);
		}
		
	}
	
	@XStreamAlias("Hierarchy")
	public static class Hierarchy extends MondrianSchemaObject {
		
		@XStreamAlias("Table")
		private Table table;
		
		@XStreamImplicit
		private List<Level> levels = new ArrayList<Level>();
		
		public Hierarchy(String name) {
			super(name);
		}
		
	}
	
	@XStreamAlias("Level")
	public static class Level extends MondrianSchemaObject {
		
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
	public static class Measure extends MondrianSchemaObject {

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
}
