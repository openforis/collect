package org.openforis.collect.relational.model;

import java.sql.Types;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition.Type;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.openforis.idm.path.Path;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */

// ASAP:
// TODO finish schema generation (to Postgresql)
// TODO Collect RDB <==> jOOQ adapters
// TODO insert data into database with jOOQ adapters

// Later:
// TODO insert dates and times
// TODO SRS table
// TODO 
// TODO Code list tables
// TODO Move schema config to RelationalSchemaConfig
public class RelationalSchemaGenerator {
	private static final String RDB_NAMESPACE = "http://www.openforis.org/collect/3.0/rdb";
	private static final QName TABLE_NAME_QNAME = new QName(RDB_NAMESPACE, "table");
	private static final QName COLUMN_NAME_QNAME = new QName(RDB_NAMESPACE, "column");
	private String idColumnSuffix = "_id";
	private String pkConstraintPrefix = "pk_";
	private String fkConstraintPrefix = "fk_";
	private String codeListTableSuffix = "_code";
	private String codeListTablePrefix = "";
	private String labelColumnMiddleSuffix = "_label_";
	private String dataTablePrefix = "";
	private String otherColumnSuffix = "_other";
	private int textMaxLength = 255;
	private int memoMaxLength = 2048;
	private int floatingPointPrecision = 24;
	
	public RelationalSchema generateSchema(Survey survey, String schemaName) throws CollectRdbException {
		RelationalSchema rs = new RelationalSchema(survey, schemaName);
		addCodeListTables(rs);
		addDataTables(rs);
		return rs;
	}
	
	public String getIdColumnSuffix() {
		return idColumnSuffix;
	}

	public void setIdColumnSuffix(String idColumnSuffix) {
		this.idColumnSuffix = idColumnSuffix;
	}

	public String getOtherColumnSuffix() {
		return otherColumnSuffix;
	}

	public void setOtherColumnSuffix(String otherColumnSuffix) {
		this.otherColumnSuffix = otherColumnSuffix;
	}

	public int getTextMaxLength() {
		return textMaxLength;
	}

	public void setTextMaxLength(int textMaxLength) {
		this.textMaxLength = textMaxLength;
	}

	public int getMemoMaxLength() {
		return memoMaxLength;
	}

	public void setMemoMaxLength(int memoMaxLength) {
		this.memoMaxLength = memoMaxLength;
	}

	public int getFloatingPointPrecision() {
		return floatingPointPrecision;
	}

	public void setFloatingPointPrecision(int floatingPointPrecision) {
		this.floatingPointPrecision = floatingPointPrecision;
	}

	public String getPkConstraintPrefix() {
		return pkConstraintPrefix;
	}

	public void setPkConstraintPrefix(String pkConstraintPrefix) {
		this.pkConstraintPrefix = pkConstraintPrefix;
	}

	public String getFkConstraintPrefix() {
		return fkConstraintPrefix;
	}

	public void setFkConstraintPrefix(String fkConstraintPrefix) {
		this.fkConstraintPrefix = fkConstraintPrefix;
	}

	public String getDataTablePrefix() {
		return dataTablePrefix;
	}

	public void setDataTablePrefix(String dataTablePrefix) {
		this.dataTablePrefix = dataTablePrefix;
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
		if ( StringUtils.isBlank(codeList.getLookupTable()) ) {
			List<CodeListLevel> hierarchy = codeList.getHierarchy();
			if ( hierarchy.size() == 0 ) {
				CodeListTable table = createCodeListTable(rs, codeList, null, null);
				rs.addTable(table);
			}
			CodeListTable parent = null;
			for (int hierarchyIdx = 0; hierarchyIdx < hierarchy.size(); hierarchyIdx++) {
				CodeListTable table = createCodeListTable(rs, codeList, parent,
						hierarchyIdx);
				rs.addTable(table);
				parent = table;
			}
		}
	}

	protected CodeListTable createCodeListTable(RelationalSchema rs,
			CodeList codeList, CodeListTable parent, Integer hierarchyIdx)
			throws CollectRdbException {
		String tableNamePrefix = getCodeListTableNamePrefix(codeList, hierarchyIdx);
		String tableName = tableNamePrefix + codeListTableSuffix;
		CodeListTable table = new CodeListTable(codeListTablePrefix, tableName, codeList, parent);
		if ( rs.containsTable(tableName) ) {
			throw new CollectRdbException("Duplicate table '"+tableName+"' for CodeList "+codeList.getName());
		}
		addPKColumn(table);
		//add code column
		CodeColumn codeColumn = new CodeColumn(tableNamePrefix);
		table.addColumn(codeColumn);
		if ( parent != null ) {
			String parentName = parent.getName();
			// Create Parent FK column
			Column<?> parentIdColumn = new CodeListParentKeyColumn(parentName + idColumnSuffix);
			table.addColumn(parentIdColumn);
			// Create FK constraint
			String fkConstraintName = fkConstraintPrefix + table.getBaseName() + "_" + parent.getBaseName();
			PrimaryKeyConstraint parentPkConstraint = parent.getPrimaryKeyConstraint();
			ReferentialConstraint fkConstraint = new ReferentialConstraint(fkConstraintName, table, parentPkConstraint, parentIdColumn);
			table.addConstraint(fkConstraint);
		}
		//add label columns
		Survey survey = codeList.getSurvey();
		List<String> langCodes = survey.getLanguages();
		for (String langCode : langCodes) {
			String labelColName = tableNamePrefix + labelColumnMiddleSuffix + langCode;
			LabelColumn labelCol = new LabelColumn(langCode, labelColName);
			table.addColumn(labelCol);
		}
		//TODO add description columns
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

	private DataTable createDataTable(RelationalSchema rs, DataTable parentTable, NodeDefinition defn, Path relativePath) throws CollectRdbException {
		String name = getDataTableName(parentTable, defn);
		DataTable table = new DataTable(dataTablePrefix, name, parentTable, defn, relativePath);
		if ( rs.containsTable(name) ) {
			throw new CollectRdbException("Duplicate table '"+name+"' for "+defn.getPath());
		}
		addPKColumn(table);
		if ( parentTable != null ) {
			// Create FK column
			String fkColumnName = parentTable.getBaseName() + idColumnSuffix;
			Column<?> fkColumn = new DataParentKeyColumn(fkColumnName);
			table.addColumn(fkColumn);
			// Create FK constraint
			String fkConstraintName = fkConstraintPrefix + table.getBaseName() + "_" + parentTable.getBaseName();
			PrimaryKeyConstraint parentPKConstraint = parentTable.getPrimaryKeyConstraint();
			ReferentialConstraint fkConstraint = new ReferentialConstraint(fkConstraintName, table, parentPKConstraint, fkColumn);
			table.addConstraint(fkConstraint);
			// Attach to parent table
			parentTable.addChildTable(table);
		}
		return table;
	}

	protected void addPKColumn(DataTable table) {
		String name = table.getName() + idColumnSuffix;
		// Create PK column
		Column<?> pkColumn = new DataPrimaryKeyColumn(name);
		table.addColumn(pkColumn);
		// Create PK constraint
		addPKConstraint(table, pkColumn);
	}
	
	protected void addPKColumn(CodeListTable table) {
		String name = table.getName() + idColumnSuffix;
		// Create PK column
		Column<?> pkColumn = new CodeListPrimaryKeyColumn(name);
		table.addColumn(pkColumn);
		// Create PK constraint
		addPKConstraint(table, pkColumn);
	}

	protected void addPKConstraint(AbstractTable<?> table, Column<?> pkColumn) {
		String pkConstraintName = pkConstraintPrefix + table.getBaseName();
		PrimaryKeyConstraint pkConstraint = new PrimaryKeyConstraint(pkConstraintName, table, pkColumn);
		table.setPrimaryKeyConstraint(pkConstraint);
	}
	
	private String getDataTableName(DataTable parentTable, NodeDefinition defn) {
		String name = defn.getAnnotation(TABLE_NAME_QNAME);
		if ( name == null ) {
			NodeDefinition parentDefn = parentTable == null ? null : parentTable.getNodeDefinition();
			StringBuilder sb = new StringBuilder();
			NodeDefinition ptr = defn;
			while ( ptr != parentDefn ) {
				if ( sb.length() > 0 ) {
					sb.insert(0 ,'_');
				}
				sb.insert(0, ptr.getName());
				ptr = ptr.getParentDefinition();
			}
			// For multiple attribute tables, prepend parent table name to table name 
			if ( defn instanceof AttributeDefinition ) {
				sb.insert(0, '_');
				sb.insert(0, parentTable.getBaseName());
			}
			name = sb.toString();
		}
		return name;
	}
	
	private String getCodeListTableNamePrefix(CodeList codeList, Integer levelIdx) {
		String name;
		if ( levelIdx == null ) {
			name = codeList.getAnnotation(TABLE_NAME_QNAME);
			if ( name == null ) {
				name = codeList.getName();
			}
		} else {
			List<CodeListLevel> hierarchy = codeList.getHierarchy();
			StringBuilder sb = new StringBuilder();
			for ( int idx = 0; idx <= levelIdx; idx ++ ) {
				CodeListLevel currentLevel = hierarchy.get(idx);
				sb.append(currentLevel.getName());
				if ( idx < levelIdx ) {
					sb.append("_");
				}
			}
			name = sb.toString();
		}
		return name;
	}

	private void addDataColumns(RelationalSchema rs, DataTable table, AttributeDefinition defn, Path relativePath) throws CollectRdbException {
		List<FieldDefinition<?>> fieldDefinitions = defn.getFieldDefinitions();
		if ( defn instanceof CodeAttributeDefinition ) {
			addDataColumns(rs, table, (CodeAttributeDefinition) defn, relativePath);
		} else if ( defn instanceof NumericAttributeDefinition ) {
			addDataColumns(table, (NumericAttributeDefinition) defn, relativePath);
		} else if ( defn instanceof DateAttributeDefinition || 
					defn instanceof TimeAttributeDefinition ) {
			addDataColumn(table, defn, relativePath);
		} else {
			for (FieldDefinition<?> field : fieldDefinitions) {
				addDataColumn(table, field, relativePath);
			}
		}
	}
	
	private void addDataColumns(RelationalSchema rs, DataTable table, CodeAttributeDefinition defn, Path relativePath) throws CollectRdbException {
		List<FieldDefinition<?>> fieldDefinitions = defn.getFieldDefinitions();
		addDataColumn(table, fieldDefinitions.get(0), relativePath);
		CodeListItemFKColumn codeListItemFKColumn = createCodeListItemFKColumn(rs, table, defn, relativePath);
		table.addColumn(codeListItemFKColumn);
		CodeList list = defn.getList();
		if ( list.isQualifiable() ) {
			addDataColumn(table, fieldDefinitions.get(1), relativePath);			
		}
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
	
	private void addDataColumn(DataTable table, FieldDefinition<?> defn, Path relativePath) throws CollectRdbException {
		relativePath = relativePath.appendElement(defn.getName());
		addDataColumn(table, (NodeDefinition) defn, relativePath);
	}
	
	private void addDataColumn(DataTable table, NodeDefinition defn, Path relativePath) throws CollectRdbException {
		DataColumn column = createDataColumn(table, defn, relativePath);
		String name = column.getName();
		if ( table.containsColumn(name) ) {
			throw new CollectRdbException("Duplicate column '"+name+"' in table '"+table.getName()+"'");
		}		
		table.addColumn(column);
	}

	private String getDataColumnName(DataTable table, AttributeDefinition defn) {		
		String name = defn.getAnnotation(COLUMN_NAME_QNAME);		
		if ( name == null ) {
			String prefix = getDataColumnNamePrefix(table, defn);
			return prefix + defn.getName();
		}
		return name;
	}

	private String getDataColumnName(DataTable table, FieldDefinition<?> fld) {
		AttributeDefinition attr = fld.getAttributeDefinition();
		if ( table.getNodeDefinition() == attr ) {
			return getAttributeTableColumnName(table, fld);
		} else {
			String name = getDataColumnName(table, attr);
			String suffix = getDataColumnSuffix((FieldDefinition<?>) fld);
			return name + suffix;
		}
	}
	
	private String getDataColumnNamePrefix(DataTable table, AttributeDefinition defn) {
		StringBuilder sb = new StringBuilder();
		NodeDefinition ptr = defn.getParentDefinition();
		while ( !ptr.isMultiple() ) {
			sb.insert(0, '_');
			sb.insert(0, ptr.getName());
			
			ptr = ptr.getParentDefinition();
		}
		return sb.toString();
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

	private CodeListItemFKColumn createCodeListItemFKColumn(RelationalSchema rs, DataTable table, 
			CodeAttributeDefinition attrDefn, Path relativePath) {
		CodeList list = attrDefn.getList();
		Integer levelIdx = attrDefn.getListLevelIndex();
		String codeListTableNamePrefix = getCodeListTableNamePrefix(list, levelIdx);
		String codeListTableName = codeListTableNamePrefix + codeListTableSuffix;
		CodeListItemFKColumn col = new CodeListItemFKColumn(codeListTableName + idColumnSuffix, attrDefn, relativePath);
		table.addColumn(col);
		// Create FK constraint
		CodeListTable codeListTable = rs.getCodeListTable(list, levelIdx);
		if ( codeListTable != null ) {
			String fkConstraintName = fkConstraintPrefix + table.getBaseName() + "_" + codeListTableName;
			PrimaryKeyConstraint codeListPkConstraint = codeListTable.getPrimaryKeyConstraint();
			ReferentialConstraint fkConstraint = new ReferentialConstraint(fkConstraintName, table, codeListPkConstraint, col);
			table.addConstraint(fkConstraint);
		} else {
			//TODO reference to custom table
		}
		return col;
	}
	

	private DataColumn createDataColumn(DataTable table, FieldDefinition<?> defn, Path relativePath) {
		String name = getDataColumnName(table, (FieldDefinition<?>) defn); 
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
			length = floatingPointPrecision ;
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
					length = memoMaxLength;
				} else {
					length = textMaxLength;
				}
			} else {
				// default for string-like types (code, qualifier)
				length = textMaxLength;
			}
		} else {
			throw new UnsupportedOperationException("Unknown field type "+type);				
		}
		
		return new DataColumn(name, jdbcType, typeName, defn, relativePath, length, nullable);
	}

	private DataColumn createDataColumn(DataTable table, AttributeDefinition defn, Path relativePath) {
		String name = getDataColumnName(table, defn);;
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

	/**
	 * 
	 * @param defn
	 * @return the suffix to append to a column name in an entity tables
	 */
	private String getDataColumnSuffix(FieldDefinition<?> defn) {
		String fld = defn.getName();
		if ( fld.equals(CodeAttributeDefinition.CODE_FIELD) || 
				fld.equals(NumberAttributeDefinition.VALUE_FIELD) ) {
			return "";
		}  else if ( fld.equals("qualifier") ) {
			return otherColumnSuffix;
		} else {
			return "_" + fld;
		}
	}
	
	/**
	 * @param defn
	 * @param table 
	 * @return the column name of a field when in a multiple attribute table
	 */
	private String getAttributeTableColumnName(DataTable table, FieldDefinition<?> defn) {
		String fld = defn.getName();
		if ( fld.equals(NumberAttributeDefinition.VALUE_FIELD) ) {
			return getDataColumnName(table, defn.getAttributeDefinition());
		} else if ( fld.equals(CodeAttributeDefinition.QUALIFIER_FIELD) ) {
			return otherColumnSuffix;
		} else {
			return fld;
		}
	}
}
