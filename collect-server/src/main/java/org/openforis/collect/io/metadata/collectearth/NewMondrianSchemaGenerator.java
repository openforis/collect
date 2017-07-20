package org.openforis.collect.io.metadata.collectearth;

import static org.openforis.collect.io.metadata.collectearth.balloon.HtmlUnicodeEscaperUtil.escapeMondrianUnicode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.io.metadata.collectearth.balloon.HtmlUnicodeEscaperUtil;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.model.CodeTable;
import org.openforis.collect.relational.model.CodeValueFKColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.collect.relational.util.CodeListTables;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.NodeLabel;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition.Type;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
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
public class NewMondrianSchemaGenerator {

	private static final String BOOLEAN_DATATYPE = "Boolean";
	private static final String INTEGER_DATATYPE = "Integer";
	private static final String NUMERIC_DATATYPE = "Numeric";
	private static final String STRING_DATATYPE = "String";
	
	private static final String[] MEASURE_AGGREGATORS = new String[] { "min", "max", "avg", "sum" };

	private CollectSurvey survey;
	private RelationalSchemaConfig rdbConfig;
	private String language;
	private String dbSchemaName;
	private RelationalSchema rdbSchema;

	public NewMondrianSchemaGenerator(CollectSurvey survey, String language, String dbSchemaName,
			RelationalSchemaConfig rdbConfig) {
		this.survey = survey;
		this.language = language;
		this.rdbConfig = rdbConfig;
		this.dbSchemaName = dbSchemaName;

		this.rdbSchema = generateRdbSchema();
	}

	public String generateXMLSchema() {
		NewMondrianSchemaGenerator.Schema mondrianSchema = generateSchema();
		XStream xStream = new XStream();
		xStream.processAnnotations(NewMondrianSchemaGenerator.Schema.class);
		String xmlSchema = xStream.toXML(mondrianSchema);
		xmlSchema = xmlSchema.replaceAll( HtmlUnicodeEscaperUtil.MONDRIAN_START_UNICODE, "&");
		return xmlSchema;
	}

	private Schema generateSchema() {
		final Schema schema = new Schema(survey.getName());
		EntityDefinition rootEntityDef = survey.getSchema().getFirstRootEntityDefinition();
		rootEntityDef.traverse(new NodeDefinitionVisitor() {
			public void visit(NodeDefinition def) {
				if (def instanceof EntityDefinition && def.isMultiple()) {
					Cube cube = generateCube((EntityDefinition) def);
					schema.cubes.add(cube);
				}
			}
		});
		/*
		VirtualCube virtualCube = new VirtualCube("Survey Data");
		List<Cube> cubes = schema.cubes;
		List<CubeUsage> cubeUsages = new ArrayList<CubeUsage>(cubes.size());
		List<VirtualCubeDimension> cubeDimensions = new ArrayList<VirtualCubeDimension>();
		List<VirtualCubeMeasure> cubeMeasures = new ArrayList<VirtualCubeMeasure>();
		for (Cube cube : cubes) {
			CubeUsage cubeUsage = new CubeUsage(cube.name);
			cubeUsage.cubeName = cube.name;
			cubeUsages.add(cubeUsage);
			for (Dimension dimension : cube.dimensions) {
				VirtualCubeDimension virtualCubeDimension = new VirtualCubeDimension(dimension.name);
				virtualCubeDimension.cubeName = cube.name;
				cubeDimensions.add(virtualCubeDimension);
			}
			for (Measure measure : cube.measures) {
				VirtualCubeMeasure virtualCubeMeasure = new VirtualCubeMeasure("[Measures].[" + measure.name + "]");
				virtualCubeMeasure.cubeName = cube.name;
				cubeMeasures.add(virtualCubeMeasure);
			}
		}
		virtualCube.cubeUsages = new CubeUsages("");
		virtualCube.cubeUsages.cubeUsages = cubeUsages;
		virtualCube.virtualCubeDimensions = cubeDimensions;
		virtualCube.virtualCubeMeasures = cubeMeasures;
		schema.virtualCubes.add(virtualCube);
		 */
		return schema;
	}

	private RelationalSchema generateRdbSchema() {
		RelationalSchemaGenerator generator = new RelationalSchemaGenerator(rdbConfig);
		RelationalSchema rdbSchema = generator.generateSchema(survey, dbSchemaName);
		return rdbSchema;
	}

	private Cube generateCube(EntityDefinition multipleEntityDef) {
		Cube cube = new Cube(determineCubeName(multipleEntityDef));
		cube.caption = extractLabel(multipleEntityDef);
		DataTable dataTable = rdbSchema.getDataTable(multipleEntityDef);
		Table table = new Table(dbSchemaName, dataTable.getName() + "_view");
		cube.tables.add(table);

		addCountMeasure(cube, multipleEntityDef);

		List<EntityDefinition> viewEntityDefinitions = new ArrayList<EntityDefinition>();
		viewEntityDefinitions.addAll(multipleEntityDef.getAncestorEntityDefinitions());
		viewEntityDefinitions.add(multipleEntityDef);
		
		for (EntityDefinition entityDef : viewEntityDefinitions) {
			List<AttributeDefinition> attributes = entityDef.getNestedAttributes();
			for (AttributeDefinition attrDef : attributes) {
				if (canBeMeasured(attrDef)) {
					Dimension dimension = generateDimension(cube, attrDef);
					cube.dimensions.add(dimension);
					addMeasures(cube, attrDef);
				}
			}
		}
		if (survey.getTarget() == SurveyTarget.COLLECT_EARTH) {
			cube.measures.addAll(1, generateEarthSpecificMeasures());
		}
		return cube;
	}
	
	private String getEntityPKColumnName(EntityDefinition entityDef) {
		return rdbSchema.getDataTable(entityDef).getPrimaryKeyColumn().getName();
	}

	private List<Measure> generateEarthSpecificMeasures() {
		List<Measure> measures = new ArrayList<Measure>();
		// Expansion factor - Area
		{
			Measure measure = new Measure("area");
			measure.column = "expansion_factor";
			measure.caption = "Area (HA)";
			measure.aggregator = "sum";
			measure.datatype = NUMERIC_DATATYPE;
			measure.formatString = "###,###.00";
			measures.add(measure);
		}

		// Plot weight
		{
			Measure measure = new Measure("plot_weight");
			measure.column = "plot_weight";
			measure.caption = "Plot Weight";
			measure.aggregator = "sum";
			measure.datatype = NUMERIC_DATATYPE;
			measure.formatString = "#,###.##";
			measures.add(measure);
		}

		return measures;
	}

	private void addMeasures(Cube cube, AttributeDefinition attrDef) {
		if (attrDef instanceof NumberAttributeDefinition) {
			String attrName = attrDef.getName();
			for (String aggregator : MEASURE_AGGREGATORS) {
				Measure measure = new Measure(attrName + "_" + aggregator);
				measure.column = attrName;
				measure.caption = String.format("%s %s", extractLabel(attrDef), aggregator);
				measure.aggregator = aggregator;
				measure.datatype = NUMERIC_DATATYPE;
				measure.formatString = "#.##";
				cube.measures.add(measure);
			}
		}
	}

	private void addCountMeasure(Cube cube, EntityDefinition entityDef) {
		Measure measure = new Measure(entityDef.getName() + "_count");
		measure.column = rdbSchema.getDataTable(entityDef).getPrimaryKeyColumn().getName();
		measure.caption = escapeMondrianUnicode(extractLabel(entityDef) + " Count");
		measure.aggregator = "distinct count";
		measure.datatype = INTEGER_DATATYPE;
		cube.measures.add(measure);
	}

	private Dimension generateDimension(Cube cube, AttributeDefinition attrDef) {
		Dimension dimension = new Dimension(determineDimensionName(attrDef));
		EntityDefinition ancestorMultipleEntity = attrDef.getNearestAncestorMultipleEntity();
		
		if (ancestorMultipleEntity.isRoot()) {
			dimension.caption = extractLabel(attrDef);
		} else {
			dimension.caption = String.format("%s %s",
					extractLabel(ancestorMultipleEntity), extractLabel(attrDef));
		}

		Hierarchy hierarchy = dimension.hierarchy;
		hierarchy.table = new Table(dbSchemaName, cube.tables.get(0).name);

		if (attrDef instanceof CodeAttributeDefinition) {
			CodeAttributeDefinition codeAttrDef = (CodeAttributeDefinition) attrDef;
			
			if (! codeAttrDef.getList().isExternal()) {
				if (attrDef.isMultiple()) {
					DataTable dataTable = rdbSchema.getDataTable(attrDef);
					String parentEntityPKColumnName = getEntityPKColumnName(ancestorMultipleEntity);
					dimension.foreignKey = parentEntityPKColumnName;
					hierarchy.table = null;
					hierarchy.primaryKey = parentEntityPKColumnName;
					hierarchy.primaryKeyTable = dataTable.getName();

					Join join = new Join(null);
					String codeListTableName = CodeListTables.getTableName(rdbConfig, codeAttrDef);

					join.leftKey = dataTable.getForeignKeyCodeColumn(codeAttrDef).getName();
					join.rightKey = CodeListTables.getIdColumnName(rdbConfig, codeListTableName);

					join.tables = Arrays.asList(
							new Table(dbSchemaName, dataTable.getName()),
							new Table(dbSchemaName, codeListTableName));

					hierarchy.join = join;
				} else {
					String codeListTableName = CodeListTables.getTableName(rdbConfig, codeAttrDef);
					hierarchy.table = new Table(dbSchemaName, codeListTableName);
					DataTable dataTable = rdbSchema.getDataTable(attrDef.getParentEntityDefinition());
					CodeValueFKColumn foreignKeyCodeColumn = dataTable.getForeignKeyCodeColumn(codeAttrDef);
					dimension.foreignKey = foreignKeyCodeColumn.getName();
				}
			}
			hierarchy.levels.add(generateLevel(attrDef));
		} else if (attrDef instanceof CoordinateAttributeDefinition) {
			dimension.type = "";
			hierarchy.type = "StandardDimension";
			{
				String fieldName = CoordinateAttributeDefinition.Y_FIELD_NAME;
				Level level = new Level(dimension.name + "_" + fieldName, extractLabel(attrDef) + " - Latitude");
				level.column = attrDef.getName() + "_" + fieldName;
				hierarchy.levels.add(level);
			}
			{
				String fieldName = CoordinateAttributeDefinition.X_FIELD_NAME;
				Level level = new Level(dimension.name + "_" + fieldName, extractLabel(attrDef) + " - Longitude");
				level.column = attrDef.getName() + "_" + fieldName;
				hierarchy.levels.add(level);
			}
		} else if (attrDef instanceof DateAttributeDefinition) {
			dimension.type = "";
			hierarchy.type = "TimeDimension";
			hierarchy.allMemberName = "attrLabel";
			String[] fieldNames = new String[] { DateAttributeDefinition.YEAR_FIELD_NAME, 
					DateAttributeDefinition.MONTH_FIELD_NAME, DateAttributeDefinition.DAY_FIELD_NAME };
			for (String fieldName : fieldNames) {
				String fieldLabel = StringUtils.capitalize(fieldName);
				Level level = new Level(dimension.name + "_" + fieldName, determineLevelCaption(attrDef, fieldLabel));
				level.column = attrDef.getName() + "_" + fieldName.toLowerCase(Locale.ENGLISH);
				level.levelType = String.format("Time%ss", fieldLabel);
				level.type = NUMERIC_DATATYPE;
				hierarchy.levels.add(level);
			}
		} else if (attrDef instanceof TaxonAttributeDefinition) {
			{
				String fieldName = TaxonAttributeDefinition.CODE_FIELD_NAME;
				Level level = new Level(dimension.name + "_" + fieldName, extractLabel(attrDef) + " - Code");
				level.column = attrDef.getName() + "_" + fieldName;
				hierarchy.levels.add(level);
			}
			{
				String fieldName = TaxonAttributeDefinition.SCIENTIFIC_NAME_FIELD_NAME;
				Level level = new Level(dimension.name + "_" + fieldName, extractLabel(attrDef) + " - Scientific name");
				level.column = attrDef.getName() + "_" + fieldName;
				hierarchy.levels.add(level);
			}
		} else if (attrDef instanceof TimeAttributeDefinition) {
			dimension.type = "";
			hierarchy.type = "TimeDimension";
			hierarchy.allMemberName = "attrLabel";
			String[] fieldNames = new String[] { TimeAttributeDefinition.HOUR_FIELD, TimeAttributeDefinition.MINUTE_FIELD };
			for (String fieldName : fieldNames) {
				String fieldLabel = StringUtils.capitalize(fieldName);
				Level level = new Level(dimension.name + "_" + fieldName, determineLevelCaption(attrDef, fieldLabel));
				level.column = attrDef.getName() + "_" + fieldName.toLowerCase(Locale.ENGLISH);
				level.levelType = String.format("Time%ss", fieldLabel);
				level.type = NUMERIC_DATATYPE;
				hierarchy.levels.add(level);
			}
		} else {
			hierarchy.levels.add(generateLevel(attrDef));
		}
		return dimension;
	}

	private Level generateLevel(AttributeDefinition nodeDef) {
		String attrName = nodeDef.getName();
		Level level = new Level(determineDimensionName(nodeDef), determineLevelCaption(nodeDef));
		level.levelType = "Regular";
		if (nodeDef instanceof NumericAttributeDefinition) {
			level.type = ((NumericAttributeDefinition) nodeDef).getType() == Type.INTEGER ? INTEGER_DATATYPE : NUMERIC_DATATYPE;
		} else if (nodeDef instanceof BooleanAttributeDefinition) {
			level.type = BOOLEAN_DATATYPE;
		} else if (nodeDef instanceof CodeAttributeDefinition) {
			level.type = ((CodeAttributeDefinition) nodeDef).getList().isExternal() ? STRING_DATATYPE : INTEGER_DATATYPE;
		} else {
			level.type = STRING_DATATYPE;
		}
		if (nodeDef instanceof CodeAttributeDefinition && !((CodeAttributeDefinition) nodeDef).getList().isExternal()) {
			CodeAttributeDefinition codeDef = (CodeAttributeDefinition) nodeDef;
			CodeTable codeListTable = rdbSchema.getCodeListTable(codeDef);
			level.table = codeListTable.getName();
			level.column = codeListTable.getPrimaryKeyConstraint().getPrimaryKeyColumn().getName();
			level.nameColumn = CodeListTables.getLabelColumnName(rdbConfig, codeDef.getList(), codeDef.getLevelIndex(), language);
		} else if (nodeDef instanceof TaxonAttributeDefinition) {
			level.column = attrName + "_code";
		} else {
			level.column = attrName;
		}
		return level;
	}

	private String determineCubeName(EntityDefinition entityDef) {
		return String.valueOf(entityDef.getId());
//		return adaptPathToName(entityDef.getPath());
	}

	private String determineDimensionName(AttributeDefinition attrDef) {
		return String.valueOf(attrDef.getId());
//		return adaptPathToName(attrDef.getPath());
	}

	private String determineLevelCaption(NodeDefinition nodeDef) {
		return determineLevelCaption(nodeDef, null);
	}
	
	private String determineLevelCaption(NodeDefinition attrDef, String subLevelName) {
		EntityDefinition nearestAncestorMultipleEntity = attrDef.getNearestAncestorMultipleEntity();
		StringBuffer sb = new StringBuffer();
		if (! nearestAncestorMultipleEntity.isRoot()) {
			sb.append(extractLabel(nearestAncestorMultipleEntity));
			sb.append(' ');
		}
		sb.append(extractLabel(attrDef));
		if (subLevelName != null) {
			sb.append(" - ");
			sb.append(subLevelName);
		}
		return sb.toString();
	}

	private String extractLabel(NodeDefinition nodeDef) {
		String attrLabel = nodeDef.getFailSafeLabel(language, 
				NodeLabel.Type.REPORTING, NodeLabel.Type.INSTANCE);
		if (attrLabel == null) {
			attrLabel = nodeDef.getName();
		}
		return escapeMondrianUnicode(attrLabel);
	}

	private boolean canBeMeasured(AttributeDefinition def) {
		return (def instanceof CodeAttributeDefinition
				|| def instanceof DateAttributeDefinition
				|| def instanceof NumberAttributeDefinition
				|| def instanceof TaxonAttributeDefinition
				|| def instanceof TextAttributeDefinition
				|| def instanceof TimeAttributeDefinition)
			&& survey.getAnnotations().isIncludedInDataExport(def);
	}

	private static class MondrianSchemaObject {

		@XStreamAsAttribute
		String name;

		public MondrianSchemaObject(String name) {
			super();
			this.name = escapeMondrianUnicode(name);
		}

	}

	@XStreamAlias("Schema")
	private static class Schema extends MondrianSchemaObject {

		@XStreamImplicit
		private List<Cube> cubes = new ArrayList<Cube>();

		@XStreamImplicit
		private List<VirtualCube> virtualCubes = new ArrayList<VirtualCube>();

		public Schema(String name) {
			super(name);
		}

	}

	@XStreamAlias("Cube")
	private static class Cube extends MondrianSchemaObject {

		@XStreamAsAttribute
		public String caption;

		@XStreamAsAttribute
		public String visible = "true";

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

	@XStreamAlias("VirtualCube")
	private static class VirtualCube extends MondrianSchemaObject {

		@XStreamAlias("CubeUsages")
		private CubeUsages cubeUsages;

		@XStreamImplicit
		private List<VirtualCubeDimension> virtualCubeDimensions = new ArrayList<VirtualCubeDimension>();

		@XStreamImplicit
		private List<VirtualCubeMeasure> virtualCubeMeasures = new ArrayList<VirtualCubeMeasure>();

		public VirtualCube(String name) {
			super(name);
		}
	}

	@XStreamAlias("CubeUsages")
	private static class CubeUsages extends MondrianSchemaObject {

		@XStreamImplicit
		private List<CubeUsage> cubeUsages = new ArrayList<CubeUsage>();

		public CubeUsages(String name) {
			super(name);
		}

	}

	@XStreamAlias("CubeUsage")
	private static class CubeUsage extends MondrianSchemaObject {

		@XStreamAsAttribute
		public String cubeName;

		public CubeUsage(String name) {
			super(name);
		}
	}

	@XStreamAlias("VirtualCubeDimension")
	private static class VirtualCubeDimension extends MondrianSchemaObject {

		@XStreamAsAttribute
		public String cubeName;

		public VirtualCubeDimension(String name) {
			super(name);
		}
	}

	@XStreamAlias("VirtualCubeMeasure")
	private static class VirtualCubeMeasure extends MondrianSchemaObject {

		@XStreamAsAttribute
		public String cubeName;

		public VirtualCubeMeasure(String name) {
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
		private String caption;

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
		private String caption;

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
		private String caption;

		@XStreamAsAttribute
		private String table;

		@XStreamAsAttribute
		private String column;

		@XStreamAsAttribute
		private String nameColumn;

		@XStreamAsAttribute
		private String uniqueMembers = "true";

		@XStreamAsAttribute
		private String levelType;

		@XStreamAsAttribute
		private String type;

		@XStreamAsAttribute
		public String hideMemberIf;

		public Level(String name, String caption) {
			super(name);
			this.caption = caption;
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
