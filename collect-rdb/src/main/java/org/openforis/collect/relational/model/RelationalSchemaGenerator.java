package org.openforis.collect.relational.model;

import static org.openforis.collect.relational.util.Constants.COLUMN_NAME_QNAME;
import static org.openforis.collect.relational.util.Constants.DATA_TABLE_PK_FORMAT;
import static org.openforis.collect.relational.util.Constants.TABLE_NAME_QNAME;

import java.sql.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.util.CodeListTables;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CalculatedAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition.Type;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.openforis.idm.path.Path;
import org.openforis.idm.path.PathElement;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */

// Later:
// TODO insert dates and times
// TODO SRS table
public class RelationalSchemaGenerator {
	
	private RelationalSchemaConfig config;
	
	//transient
	private ColumnNameGenerator columnNameGenerator; 
	
	public RelationalSchemaGenerator() {
		this(RelationalSchemaConfig.createDefault());
	}
	
	public RelationalSchemaGenerator(RelationalSchemaConfig config) {
		this.config = config;
		this.columnNameGenerator = new ColumnNameGenerator(config.isUniqueColumnNames(), config.getOtherColumnSuffix());
	}

	public RelationalSchema generateSchema(Survey survey, String schemaName) throws CollectRdbException {
		RelationalSchema rs = new RelationalSchema(survey, schemaName);
		addCodeListTables(rs);
		addDataTables(rs);
		return rs;
	}
	
	private void addCodeListTables(RelationalSchema rs) throws CollectRdbException {
		Survey survey = rs.getSurvey();
		List<CodeList> codeLists = survey.getCodeLists();
		for (CodeList codeList : codeLists) {
			addCodeListTable(rs, codeList);
		}
	}

	private void addDataTables(RelationalSchema rs) throws CollectRdbException {
		Survey survey = rs.getSurvey();
		Schema schema = survey.getSchema();
		// Recursively create tables, columns and constraints
		List<EntityDefinition> roots = schema.getRootEntityDefinitions();
		for (EntityDefinition root : roots) {
			Path relativePath = Path.relative(root.getName());
			addDataObjects(rs, null, root, relativePath);
		}
	}

	private void addCodeListTable(RelationalSchema rs, CodeList codeList) throws CollectRdbException {
		if ( ! codeList.isExternal() ) {
			List<CodeListLevel> hierarchy = codeList.getHierarchy();
			if ( hierarchy.size() == 0 ) {
				CodeTable table = createCodeListTable(rs, codeList, null, null);
				rs.addTable(table);
			}
			CodeTable parent = null;
			for (int hierarchyIdx = 0; hierarchyIdx < hierarchy.size(); hierarchyIdx++) {
				CodeTable table = createCodeListTable(rs, codeList, parent,
						hierarchyIdx);
				rs.addTable(table);
				parent = table;
			}
		}
	}

	protected CodeTable createCodeListTable(RelationalSchema rs,
			CodeList codeList, CodeTable parent, Integer hierarchyIdx)
			throws CollectRdbException {
		String tableName = CodeListTables.getTableName(config, codeList, hierarchyIdx);
		CodeTable table = new CodeTable(config.getCodeListTablePrefix(), tableName, codeList, parent, 
				config.getDefaultCode(), config.getDefaultCodeLabels());
		if ( rs.containsTable(tableName) ) {
			throw new CollectRdbException("Duplicate table '"+tableName+"' for CodeList "+codeList.getName());
		}
		// Create PK column
		Column<?> pkColumn = new CodePrimaryKeyColumn(CodeListTables.getIdColumnName(config, table.getName()));
		table.addColumn(pkColumn);
		// Create PK constraint
		addPKConstraint(table, pkColumn);
		
		//add code column
		CodeListCodeColumn codeColumn = new CodeListCodeColumn(CodeListTables.getCodeColumnName(codeList, hierarchyIdx));
		table.addColumn(codeColumn);
		if ( parent != null ) {
			// Create Parent FK column
			Column<?> parentIdColumn = new CodeParentKeyColumn(CodeListTables.getIdColumnName(config, parent.getName()));
			addColumn(table, parentIdColumn);
			// Create FK constraint
			String fkConstraintName = config.getFkConstraintPrefix() + table.getBaseName() + "_" + parent.getBaseName();
			PrimaryKeyConstraint parentPkConstraint = parent.getPrimaryKeyConstraint();
			ReferentialConstraint fkConstraint = new ReferentialConstraint(fkConstraintName, table, parentPkConstraint, parentIdColumn);
			table.addConstraint(fkConstraint);
		}
		Survey survey = codeList.getSurvey();
		//add default language label column
		{
			String columnName = CodeListTables.getLabelColumnName(config, codeList, hierarchyIdx);
			CodeLabelColumn col = new CodeLabelColumn(survey.getDefaultLanguage(), columnName);
			addColumn(table, col);
		}
		
		//add label columns
		for (String langCode : survey.getLanguages()) {
			String colName = CodeListTables.getLabelColumnName(config, codeList, hierarchyIdx, langCode);
			CodeLabelColumn col = new CodeLabelColumn(langCode, colName);
			addColumn(table, col);
		}
		
		//add default language description column
		{
			String colName = CodeListTables.getDescriptionColumnName(config, codeList, hierarchyIdx);
			CodeListDescriptionColumn col = new CodeListDescriptionColumn(survey.getDefaultLanguage(), colName);
			addColumn(table, col);
		}
		
		//add description columns
		for (String langCode : survey.getLanguages()) {
			String colName = CodeListTables.getDescriptionColumnName(config, codeList, hierarchyIdx, langCode);
			CodeListDescriptionColumn col = new CodeListDescriptionColumn(langCode, colName);
			table.addColumn(col);
		}
		return table;
	}

	/**
	 * Recursively creates and adds tables and columns
	 * 
	 * @param rs
	 * @param parentTable
	 * @param defn
	 * @throws CollectRdbException
	 */
	private void addDataObjects(RelationalSchema rs, DataTable table, NodeDefinition defn, Path relativePath) throws CollectRdbException {
		if ( defn instanceof EntityDefinition  ) {
			if ( defn.isMultiple() ) {
				// Create table for multiple entity 
				table = createDataTable(rs, table, defn, relativePath);
				rs.addTable(table);
			} else {
				// just keep a reference 
				rs.assignAncestorTable((EntityDefinition) defn);
			}
				
			// Add child tables and columns
			EntityDefinition entityDefn = (EntityDefinition) defn;
			for (NodeDefinition child : entityDefn.getChildDefinitions()) {
				Path childPath;
				if ( defn.isMultiple() ) {
					childPath = Path.relative(child.getName());
				} else {
					childPath = relativePath.appendElement(child.getName());
				}
				addDataObjects(rs, table, child, childPath);
			}
		} else if ( defn instanceof AttributeDefinition ) {
			CollectSurvey survey = (CollectSurvey) defn.getSurvey();
			CollectAnnotations annotations = survey.getAnnotations();
			
			//do not include if it's a calculated attribute and it has not to be included in data export
			if ( ! (defn instanceof CalculatedAttributeDefinition) || 
					annotations.isIncludedInDataExport((CalculatedAttributeDefinition) defn) ) { 
				if ( defn.isMultiple() ) {
					// Create table for multiple attributes
					table = createDataTable(rs, table, defn, relativePath);
					rs.addTable(table);
					relativePath = Path.relative(".");
				}
				// Add columns for attributes in entity tables or attribute tables
				addDataColumns(rs, table, (AttributeDefinition) defn, relativePath);
			}
		}
	}

	private DataTable createDataTable(RelationalSchema rs, DataTable parentTable, NodeDefinition defn, Path relativePath) throws CollectRdbException {
		String name = generateDataTableName(rs, parentTable, defn);
		DataTable table = new DataTable(config.getDataTablePrefix(), name, parentTable, defn, relativePath);
		if ( rs.containsTable(name) ) {
			throw new CollectRdbException("Duplicate table '"+name+"' for "+defn.getPath());
		}
		addPKColumn(table);
		
		if ( config.isAncestorKeyColumnsIncluded() ) {
			addAncestorKeyColumns(table);
		}
		
		if ( parentTable != null ) {
			// Create FK column
			Column<?> fkColumn = new DataParentKeyColumn(getTablePKColumnName(parentTable));
			table.addColumn(fkColumn);
			// Create FK constraint
			String fkConstraintName = config.getFkConstraintPrefix() + table.getBaseName() + "_" + parentTable.getBaseName();
			PrimaryKeyConstraint parentPKConstraint = parentTable.getPrimaryKeyConstraint();
			ReferentialConstraint fkConstraint = new ReferentialConstraint(fkConstraintName, table, parentPKConstraint, fkColumn);
			table.addConstraint(fkConstraint);
			// Attach to parent table
			parentTable.addChildTable(table);
		}
		return table;
	}

	protected void addPKColumn(DataTable table) {
		String name = getTablePKColumnName(table);
		// Create PK column
		Column<?> pkColumn = new DataPrimaryKeyColumn(name);
		table.addColumn(pkColumn);
		// Create PK constraint
		addPKConstraint(table, pkColumn);
	}

	private String getTablePKColumnName(AbstractTable<?> table) {
		String result = String.format(DATA_TABLE_PK_FORMAT, table.getName(), config.getIdColumnSuffix());
		return result;
	}
	
	protected void addAncestorKeyColumns(DataTable table) throws CollectRdbException {
		NodeDefinition nodeDefn = table.getNodeDefinition();
		List<EntityDefinition> ancestors = nodeDefn.getAncestorEntityDefinitions();
		for (int levelIdx = 0; levelIdx < ancestors.size(); levelIdx++) {
			EntityDefinition ancestor = ancestors.get(levelIdx);
			List<AttributeDefinition> keyAttrDefns = ancestor.getKeyAttributeDefinitions();
			for (AttributeDefinition keyDefn : keyAttrDefns) {
				FieldDefinition<?> fieldDefn = getKeyAttributeValueFieldDefinition(keyDefn);
				Path fieldRelativePath = createAncestorKeyRelativePath(ancestors.size() - levelIdx, fieldDefn);
				String colName = getAncestorKeyColumnName(keyDefn);
				AncestorKeyColumn col = new AncestorKeyColumn(colName, fieldDefn, fieldRelativePath);
				addColumn(table, col);
			}
		}
	}
	
	protected FieldDefinition<?> getKeyAttributeValueFieldDefinition(
			AttributeDefinition defn) {
		FieldDefinition<?> fieldDefn;
		if ( defn instanceof CodeAttributeDefinition ) {
			fieldDefn = defn.getFieldDefinition(CodeAttributeDefinition.CODE_FIELD);
		} else if ( defn instanceof NumberAttributeDefinition ) {
			fieldDefn = defn.getFieldDefinition(NumberAttributeDefinition.VALUE_FIELD);
		} else if ( defn instanceof TextAttributeDefinition ) {
			fieldDefn = defn.getFieldDefinition("value"); //TODO create constant in TextAttributeDefinition
		} else {
			throw new IllegalArgumentException("Invalid key attribute definition type: " + defn.getClass().getName());
		}
		return fieldDefn;
	}

	protected String getAncestorKeyColumnName(AttributeDefinition keyDefn) {
		NodeDefinition parentDefn = keyDefn.getParentDefinition();
		return parentDefn.getName() + "_" + keyDefn.getName();
	}
	
	protected Path createAncestorKeyRelativePath(int depth, FieldDefinition<?> field) {
		Path result = Path.relative(".");
		for (int i = 0; i < depth; i++) {
			result = result.append(new PathElement(".."));
		}
		NodeDefinition parentDefn = field.getParentDefinition();
		result = result.appendElement(parentDefn.getName());
		result = result.appendElement(field.getName());
		return result;
	}
	
	protected void addPKConstraint(AbstractTable<?> table, Column<?> pkColumn) {
		String pkConstraintName = config.getPkConstraintPrefix() + table.getBaseName();
		PrimaryKeyConstraint pkConstraint = new PrimaryKeyConstraint(pkConstraintName, table, pkColumn);
		table.setPrimaryKeyConstraint(pkConstraint);
	}
	
	private String generateDataTableName(RelationalSchema rs, DataTable parentTable, NodeDefinition defn) {
		String name = defn.getAnnotation(TABLE_NAME_QNAME);
		if ( name == null ) {
			name = defn.getName();
			NodeDefinition parent = defn.getParentDefinition();
			while ( rs.containsTable(name) && parent != null ) {
				name = parent.getName() + "_" + name;
				parent = parent.getParentDefinition();
			}
			if ( rs.containsTable(name) ) {
				throw new RuntimeException(String.format("Unable to generate unique data table name for node definition %s", defn.getPath()));
			}
		}
		return name;
	}
	
	private void addDataColumns(RelationalSchema rs, DataTable table, AttributeDefinition defn, Path relativePath) throws CollectRdbException {
		if ( defn instanceof CodeAttributeDefinition ) {
			addDataColumns(rs, table, (CodeAttributeDefinition) defn, relativePath);
		} else if ( defn instanceof NumericAttributeDefinition ) {
			addDataColumns(table, (NumericAttributeDefinition) defn, relativePath);
		} else if ( defn instanceof DateAttributeDefinition || 
					defn instanceof TimeAttributeDefinition ) {
			//create date or time type column
			addDataColumn(table, defn, relativePath);
			//add even one column for each field
			addDataColumnsForEachField(table, defn, relativePath);
		} else {
			addDataColumnsForEachField(table, defn, relativePath);
		}
	}

	private void addDataColumnsForEachField(DataTable table, AttributeDefinition defn, Path relativePath) throws CollectRdbException {
		List<FieldDefinition<?>> fieldDefinitions = defn.getFieldDefinitions();
		for (FieldDefinition<?> field : fieldDefinitions) {
			addDataColumn(table, field, relativePath);
		}
	}
	
	private void addDataColumns(RelationalSchema rs, DataTable table, CodeAttributeDefinition defn, Path relativePath) throws CollectRdbException {
		FieldDefinition<?> codeField = defn.getFieldDefinition(CodeAttributeDefinition.CODE_FIELD);
		addCodeColumn(table, codeField, relativePath);
		CodeList list = defn.getList();
		if ( ! list.isExternal() ) {
			addCodeValueFKColumn(rs, table, defn, relativePath);
		}
		boolean qualifiable = isQualifiable(list);
		if ( qualifiable ) {
			addDataColumn(table, defn.getFieldDefinition(CodeAttributeDefinition.QUALIFIER_FIELD), relativePath);			
		}
	}
	
	protected boolean isQualifiable(CodeList list) {
		boolean qualifiable = false;
		if ( list.isExternal() ) {
			return false;
		} else if ( list.isEmpty() ) {
			Survey survey = list.getSurvey();
			SurveyContext context = survey.getContext();
			CodeListService codeListService = context.getCodeListService();
			qualifiable = codeListService.hasQualifiableItems(list);
		} else {
			qualifiable = list.isQualifiable();
		}
		return qualifiable;
	}
	
	private void addDataColumns(DataTable table, NumericAttributeDefinition defn, Path relativePath) throws CollectRdbException {
		List<FieldDefinition<?>> fieldDefinitions = defn.getFieldDefinitions();
		boolean variableUnit = defn.isVariableUnit();
		for (FieldDefinition<?> field : fieldDefinitions) {
			String name = field.getName();
			if ( variableUnit || ! 
					(name.equals(NumberAttributeDefinition.UNIT_FIELD) || name.equals(NumberAttributeDefinition.UNIT_NAME_FIELD)) ) {
				addDataColumn(table, field, relativePath);
			}
		}
	}	
	
	private void addCodeColumn(DataTable table, FieldDefinition<?> defn, Path relativePath) throws CollectRdbException {
		relativePath = relativePath.appendElement(defn.getName());
		String name = columnNameGenerator.generateName(defn);
		CodeColumn column = new CodeColumn(name, defn, relativePath,  config.getTextMaxLength(), config.getDefaultCode()); 
		addColumn(table, column);
	}
	
	private void addDataColumn(DataTable table, FieldDefinition<?> defn, Path relativePath) throws CollectRdbException {
		relativePath = relativePath.appendElement(defn.getName());
		addDataColumn(table, (NodeDefinition) defn, relativePath);
	}
	
	private void addDataColumn(DataTable table, NodeDefinition defn, Path relativePath) throws CollectRdbException {
		DataColumn column = createDataColumn(table, defn, relativePath);
		addColumn(table, column);
	}

	private void addColumn(AbstractTable<?> table, Column<?> column)
			throws CollectRdbException {
		String name = column.getName();
		if ( table.containsColumn(name) ) {
			throw new CollectRdbException("Duplicate column '"+name+"' in table '"+table.getName()+"'");
		}		
		table.addColumn(column);
	}

	private DataColumn createDataColumn(DataTable table, NodeDefinition defn, Path relativePath) {
		if ( defn instanceof FieldDefinition ) {
			return createDataColumn(table, (FieldDefinition<?>) defn, relativePath);
		} else if ( defn instanceof AttributeDefinition ) {
			return createDataColumn(table, (AttributeDefinition) defn, relativePath);
		} else {
			throw new UnsupportedOperationException("Unknown defn "+defn.getClass());			
		}
	}

	private void addCodeValueFKColumn(RelationalSchema rs, DataTable table, 
			CodeAttributeDefinition attrDefn, Path relativePath) throws CollectRdbException {
		CodeList list = attrDefn.getList();
		Integer levelIdx = attrDefn.getListLevelIndex();
		String codeListTableName = CodeListTables.getTableName(config, list, levelIdx);
		DataColumn codeValueColumn = table.getDataColumn(attrDefn.getFieldDefinition(CodeAttributeDefinition.CODE_FIELD));
		String codeValueColumnName = codeValueColumn.getName();
		String fkColumnName = codeValueColumnName + config.getCodeListTableSuffix() + config.getIdColumnSuffix();
		CodeValueFKColumn col = new CodeValueFKColumn(fkColumnName, attrDefn, relativePath,
				config.getDefaultCode());
		addColumn(table, col);
		// Create FK constraint
		CodeTable codeListTable = rs.getCodeListTable(list, levelIdx);
		if ( codeListTable != null ) {
			String fkConstraintName = config.getFkConstraintPrefix() + codeValueColumnName + "_" + codeListTableName;
			PrimaryKeyConstraint codeListPkConstraint = codeListTable.getPrimaryKeyConstraint();
			ReferentialConstraint fkConstraint = new ReferentialConstraint(fkConstraintName, table, codeListPkConstraint, col);
			table.addConstraint(fkConstraint);
		}
	}

	private DataColumn createDataColumn(DataTable table, FieldDefinition<?> defn, Path relativePath) {
		String name = columnNameGenerator.generateName(defn); 
		int jdbcType;
		String typeName;
		Integer length = null;
		boolean nullable = true;
		
		Class<?> type = defn.getValueType();
		if ( type == Integer.class ) {
			jdbcType = Types.INTEGER;
			typeName =  "integer";
		} else if ( type == Double.class ) {
			jdbcType = Types.FLOAT;
			typeName =  "float";
			length = config.getFloatingPointPrecision();
		} else if ( type == Long.class ) {
			jdbcType = Types.BIGINT;
			typeName = "bigint";
		} else if ( type == Boolean.class ) {
			jdbcType = Types.BOOLEAN;
			typeName =  "boolean";
		} else if ( type == String.class ) {
			jdbcType = Types.VARCHAR;
			typeName =  "varchar";
			AttributeDefinition attr = ((FieldDefinition<?>) defn).getAttributeDefinition();
			if ( attr instanceof TextAttributeDefinition ) {
				TextAttributeDefinition textAttr = (TextAttributeDefinition) attr;
				if ( textAttr.getType() == Type.MEMO ) {
					length = config.getMemoMaxLength();
				} else {
					length = config.getTextMaxLength();
				}
			} else {
				// default for string-like types (code, qualifier)
				length = config.getTextMaxLength();
			}
		} else {
			throw new UnsupportedOperationException("Unknown field type "+type);				
		}
		
		return new DataColumn(name, jdbcType, typeName, defn, relativePath, length, nullable);
	}

	/**
	 * Creates a single data column for the specified node definition.
	 * The column type depends on the {@link NodeDefinition} type.
	 * - for a {@link FieldDefinition} it depends on the field type (numeric, boolean or text).
	 * - for a {@link DateAttributeDefinition} the type is "date"
	 * - for a {@link TimeAttributeDefinition} the type is "time"
	 */
	private DataColumn createDataColumn(DataTable table, AttributeDefinition defn, Path relativePath) {
		String name = columnNameGenerator.generateName(defn);
		int jdbcType;
		String typeName;
		Integer length = null;
		boolean nullable = true;
		
		if ( defn instanceof DateAttributeDefinition ) {
			jdbcType = Types.DATE;
			typeName =  "date";
		} else if ( defn instanceof TimeAttributeDefinition ) {
			jdbcType = Types.TIME;
			typeName =  "time";
		} else {
			throw new UnsupportedOperationException("Unknown defn "+defn.getClass());
		}
	
		return new DataColumn(name, jdbcType, typeName, defn, relativePath, length, nullable);
	}

	public void setConfig(RelationalSchemaConfig config) {
		this.config = config;
	}
	
	class ColumnNameGenerator {
		
		private boolean uniqueNames;
		private String otherColumnSuffix;
		private Set<String> uniqueColumnNames;
		
		public ColumnNameGenerator(boolean uniqueNames, String otherColumnSuffix) {
			this.uniqueNames = uniqueNames;
			this.otherColumnSuffix = otherColumnSuffix;
			this.uniqueColumnNames = new HashSet<String>();
		}

		public String generateName(NodeDefinition node) {
			AttributeDefinition attr = (node instanceof AttributeDefinition ? (AttributeDefinition) node: 
				((FieldDefinition<?>) node).getAttributeDefinition());
			String baseName = attr.getAnnotation(COLUMN_NAME_QNAME);		
			if ( baseName == null ) {
				baseName = attr.getName();
			}
			String completeName = getCompleteName(node, baseName);
			if ( uniqueNames ) {
				//if the name is not unique, prepend ancestor names
				if ( uniqueColumnNames.contains(completeName) ) {
					EntityDefinition parent = attr.getParentEntityDefinition();
					while ( parent != null &&  uniqueColumnNames.contains(completeName) ) {
						baseName = parent.getName() + "_" + baseName;
						completeName = getCompleteName(node, baseName);
						parent = parent.getParentEntityDefinition();
					}
				}
				//if the name is still not unique, append a number to the name
				if ( uniqueColumnNames.contains(completeName) ) {
					String tempBaseName = baseName;
					completeName = getCompleteName(node, tempBaseName);
					int count = 1;
					while ( uniqueColumnNames.contains(completeName) ) {
						baseName = tempBaseName + (count++);
						completeName = getCompleteName(node, baseName);
					}
				}
				uniqueColumnNames.add(completeName);
			}
			return completeName; 
		}

		private String getCompleteName(NodeDefinition node, String baseName) {
			String suffix = node instanceof FieldDefinition ? getFieldNameSuffix((FieldDefinition<?>) node): "";
			return baseName + suffix;
		}
		
		/**
		 * 
		 * @param defn
		 * @return the suffix to append to a column name in multiple entity tables
		 */
		public String getFieldNameSuffix(FieldDefinition<?> defn) {
			String fldName = defn.getName();
			AttributeDefinition attrDefn = defn.getAttributeDefinition();
			if ( attrDefn instanceof BooleanAttributeDefinition ||
				attrDefn instanceof TextAttributeDefinition ||
				attrDefn instanceof CalculatedAttributeDefinition && fldName.equals(attrDefn.getMainFieldName() ) || 
				attrDefn instanceof CodeAttributeDefinition && fldName.equals(CodeAttributeDefinition.CODE_FIELD) || 
				attrDefn instanceof NumberAttributeDefinition && fldName.equals(NumberAttributeDefinition.VALUE_FIELD)
				) {
				return "";
			}  else if ( attrDefn instanceof CodeAttributeDefinition && fldName.equals(CodeAttributeDefinition.QUALIFIER_FIELD) ) {
				return otherColumnSuffix;
			} else {
				return "_" + fldName;
			}
		}

	}
}

