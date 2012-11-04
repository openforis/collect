package org.openforis.collect.relational;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Types;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.namespace.QName;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition.Type;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.RealAttribute;
import org.openforis.idm.model.Time;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.path.Path;

/**
 * 
 * @author G. Miceli
 *
 */
public class RelationalSchemaGenerator {
	private static final String RDB_NAMESPACE = "http://www.openforis.org/collect/3.0/rdb";
	private static final QName TABLE_NAME_QNAME = new QName(RDB_NAMESPACE, "table");
	private static final QName COLUMN_NAME_QNAME = new QName(RDB_NAMESPACE, "column");
	private String idColumnName = "id";
	private String pkConstraintPrefix = "pk_";
	private String fkConstraintPrefix = "fk_";
	private String dataTablePrefix = "fd_";
	
	public RelationalSchema generateSchema(Survey survey) throws SchemaGenerationException {
		RelationalSchema rs = new RelationalSchema(survey);
		addCodeListTables(rs);
		addDataTables(rs);
		return rs;
	}

	
	public String getIdColumnName() {
		return idColumnName;
	}


	public void setIdColumnName(String idColumnName) {
		this.idColumnName = idColumnName;
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

	private void addCodeListTables(RelationalSchema rs) throws SchemaGenerationException {
		// TODO Auto-generated method stub
		
	}

	private void addDataTables(RelationalSchema rs) throws SchemaGenerationException {
		Survey survey = rs.getSurvey();
		Schema schema = survey.getSchema();
		// Recursively create tables, columns and constraints
		List<EntityDefinition> roots = schema.getRootEntityDefinitions();
		for (EntityDefinition root : roots) {
			Path relativePath = Path.relative(root.getName());
			addDataObjects(rs, null, root, relativePath);
		}
	}

	/**
	 * Recursively creates and adds tables and columns
	 * 
	 * @param rs
	 * @param parentTable
	 * @param defn
	 * @throws SchemaGenerationException
	 */
	private void addDataObjects(RelationalSchema rs, DataTable table, NodeDefinition defn, Path relativePath) throws SchemaGenerationException {
		if ( defn instanceof EntityDefinition  ) {
			// Create table for all entities 
			table = createDataTable(rs, table, defn, relativePath);
			rs.addTable(table);
			
			// Add child tables and columns
			EntityDefinition entityDefn = (EntityDefinition) defn;
			for (NodeDefinition child : entityDefn.getChildDefinitions()) {
				relativePath = Path.relative(child.getName());
				addDataObjects(rs, table, child, relativePath);
			}
		} else if ( defn instanceof AttributeDefinition ) {
			if ( defn.isMultiple() ) {
				// Create table for multiple attributes
				table = createDataTable(rs, table, defn, relativePath);
				rs.addTable(table);
				relativePath = Path.relative(".");
			}
			// Add columns for attributes in entity tables or attribute tables
			addDataColumns(table, (AttributeDefinition) defn, relativePath);
		}
	}

	private DataTable createDataTable(RelationalSchema rs, DataTable parentTable, NodeDefinition defn, Path relativePath)
			throws SchemaGenerationException {
		String name = getDataTableName(parentTable, defn);
		DataTable table = new DataTable(dataTablePrefix, name, parentTable, defn, relativePath);
		if ( rs.containsTable(name) ) {
			throw new SchemaGenerationException("Duplicate table '"+name+"' for "+defn.getPath());
		}
		// Create PK column
		Column<?> pkColumn = new DataPrimaryKeyColumn(idColumnName);
		table.addColumn(pkColumn);
		// Create PK constraint
		String pkConstraintName = pkConstraintPrefix+table.getName();
		PrimaryKeyConstraint pkConstraint = new PrimaryKeyConstraint(pkConstraintName, table, pkColumn);
		table.addConstraint(pkConstraint);
		if ( parentTable != null ) {
			// Create FK column
			String fkColumnName = parentTable.getName()+"_"+idColumnName;
			Column<?> fkColumn = new DataParentKeyColumn(fkColumnName);
			table.addColumn(fkColumn);
			// Create FK constraint
			String fkConstraintName = fkConstraintPrefix+parentTable.getName();
			ReferentialConstraint fkConstraint = new ReferentialConstraint(fkConstraintName, table, pkConstraint, fkColumn);
			table.addConstraint(fkConstraint);
			// Attach to parent table
			parentTable.addChildTable(table);
		}
		return table;
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
				sb.insert(0, parentTable.getName());
			}
			name = sb.toString();
		}
		return name;
	}

	private void addDataColumns(DataTable table, AttributeDefinition defn, Path relativePath) throws SchemaGenerationException {
		List<FieldDefinition<?>> fieldDefinitions = defn.getFieldDefinitions();
		if ( defn instanceof CodeAttributeDefinition ) {
			addDataColumns(table, (CodeAttributeDefinition) defn, relativePath);
		} else if ( defn instanceof NumericAttributeDefinition ) {
			addDataColumns(table, (NumericAttributeDefinition) defn, relativePath);
		} else if ( defn instanceof DateAttributeDefinition || 
					defn instanceof TimeAttributeDefinition || 
					defn instanceof CoordinateAttributeDefinition ) {
			addDataColumn(table, defn, relativePath);
		} else {
			for (FieldDefinition<?> field : fieldDefinitions) {
				addDataColumn(table, field, relativePath);
			}
		}
	}
	
	private void addDataColumns(DataTable table, CodeAttributeDefinition defn, Path relativePath) throws SchemaGenerationException {
		List<FieldDefinition<?>> fieldDefinitions = defn.getFieldDefinitions();
		addDataColumn(table, fieldDefinitions.get(0), relativePath);
		CodeList list = defn.getList();
		if ( list.isQualifiable() ) {
			addDataColumn(table, fieldDefinitions.get(1), relativePath);			
		}
	}
	
	private void addDataColumns(DataTable table, NumericAttributeDefinition defn, Path relativePath) throws SchemaGenerationException {
		List<FieldDefinition<?>> fieldDefinitions = defn.getFieldDefinitions();
		boolean variableUnit = defn.isVariableUnit();
		for (FieldDefinition<?> field : fieldDefinitions) {
			if ( variableUnit || !field.getName().equals("unit") ) {
				addDataColumn(table, field, relativePath);
			}
		}
	}	
	
	private void addDataColumn(DataTable table, FieldDefinition<?> defn, Path relativePath) throws SchemaGenerationException {
		relativePath = relativePath.appendElement(defn.getName());
		addDataColumn(table, (NodeDefinition) defn, relativePath);
	}
	
	private void addDataColumn(DataTable table, NodeDefinition defn, Path relativePath) throws SchemaGenerationException {
		String name = getDataColumnName(table, defn);
		int type = getDataColumnType(defn);
		Integer length = getDataColumnLength(defn);
		DataColumn column = new DataColumn(name, type, length, true, defn, relativePath);
		if ( table.containsColumn(name) ) {
			throw new SchemaGenerationException("Duplicate column '"+name+"' in table '"+table.getName()+"'");
		}

		table.addColumn(column);
	}

	private int getDataColumnType(NodeDefinition defn) {
		if ( defn instanceof FieldDefinition ) {
			Class<?> type = ((FieldDefinition<?>) defn).getValueType();
			if ( type == Integer.class ) {
				return Types.INTEGER;
			} else if ( type == Double.class ) {
				return Types.FLOAT;
			} else if ( type == Boolean.class ) {
				return Types.BOOLEAN;
			} 
		} else if ( defn instanceof DateAttributeDefinition ) {
			return Types.DATE;
		} else if ( defn instanceof TimeAttributeDefinition ) {
			return Types.TIME;
		}
		return Types.VARCHAR;
	}

	private Integer getDataColumnLength(NodeDefinition defn) {
		if ( defn instanceof FieldDefinition<?> ) {
			FieldDefinition<?> fld = (FieldDefinition<?>) defn;
			Class<?> type = fld.getValueType();
			if ( type == String.class ) {
				AttributeDefinition attr = fld.getAttributeDefinition();
				if ( attr instanceof TextAttributeDefinition ) {
					TextAttributeDefinition textAttr = (TextAttributeDefinition) attr;
					if ( textAttr.getType() == Type.MEMO ) {
						return 2048;
					}
				} 
				return 255;
			} 
		} else if ( defn instanceof CoordinateAttributeDefinition ) {
			return 255;
		} 
		return null;
	}

	private String getDataColumnName(DataTable table, NodeDefinition defn) {
		if ( defn instanceof AttributeDefinition ) {
			return getDataColumnName(table, (AttributeDefinition) defn); 
		} else if ( defn instanceof FieldDefinition ) {
			return getDataColumnName(table, (FieldDefinition<?>) defn);
		} else {
			throw new UnsupportedOperationException("Column "+defn.getClass());
		}
	}
	
	private String getDataColumnName(DataTable table, AttributeDefinition defn) {
		String name = defn.getAnnotation(COLUMN_NAME_QNAME);
		if ( name == null ) {
			return defn.getName();
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
			if ( suffix != null ) {
				name = name +"_" + suffix;
			}
			return name;
		}
	}
	
	/**
	 * 
	 * @param defn
	 * @return the suffix to append to a column name in an entity tables
	 */
	private String getDataColumnSuffix(FieldDefinition<?> defn) {
		String fld = defn.getName();
		if ( fld .equals("code") || fld .equals("value") ) {
			return null;
		}  else if ( fld.equals("qualifier") ) {
			return "other";
		} else {
			return fld;
		}
	}
	
	/**
	 * @param defn
	 * @param table 
	 * @return the column name of a field when in a multiple attribute table
	 */
	private String getAttributeTableColumnName(DataTable table, FieldDefinition<?> defn) {
		String fld = defn.getName();
		if ( fld.equals("value") ) {
			return getDataColumnName(table, defn.getAttributeDefinition());
		} else if ( fld.equals("qualifier") ) {
			return "other";
		} else {
			return fld;
		}
	}

	/// DEV
	
	
	private static CollectSurvey loadSurvey() throws IdmlParseException, FileNotFoundException {
//		InputStream is = ClassLoader.getSystemResourceAsStream("test.idm.xml");
//		InputStream is = new FileInputStream("/home/gino/workspace/of/idm/idm-test/src/main/resources/test.idm.xml");
		InputStream is = new FileInputStream("D:/data/workspace/idm/idm-test/src/main/resources/test.idm.xml");
//		InputStream is = new FileInputStream("/home/gino/workspace/faofin/tz/naforma-idm/tanzania-naforma.idm.xml");
		CollectSurveyContext ctx = new CollectSurveyContext(new ExpressionFactory(), null, null);
		CollectSurveyIdmlBinder binder = new CollectSurveyIdmlBinder(ctx);
		return (CollectSurvey) binder.unmarshal(is);
	}


	private static CollectRecord createTestRecord(CollectSurvey survey, String id) {
		CollectRecord record = new CollectRecord(survey, "2.0");
		Entity cluster = record.createRootEntity("cluster");
		record.setCreationDate(new GregorianCalendar(2011, 12, 31, 23, 59).getTime());
		//record.setCreatedBy("ModelDaoIntegrationTest");
		record.setStep(Step.ENTRY);

		addTestValues(cluster, id);
			
		//set counts
		record.getEntityCounts().add(2);
		
		//set keys
		record.getRootEntityKeyValues().add(id);
		
		return record;
	}

	private static void addTestValues(Entity cluster, String id) {
//		cluster.setId(100);
		EntityBuilder.addValue(cluster, "id", new Code(id));
		EntityBuilder.addValue(cluster, "gps_realtime", Boolean.TRUE);
		EntityBuilder.addValue(cluster, "region", new Code("001"));
		EntityBuilder.addValue(cluster, "district", new Code("002"));
		EntityBuilder.addValue(cluster, "crew_no", 10);
		EntityBuilder.addValue(cluster, "map_sheet", "value 1");
		EntityBuilder.addValue(cluster, "map_sheet", "value 2");
		EntityBuilder.addValue(cluster, "vehicle_location", new Coordinate((double)432423423l, (double)4324324l, "srs"));
		EntityBuilder.addValue(cluster, "gps_model", "TomTom 1.232");
		{
			Entity ts = EntityBuilder.addEntity(cluster, "time_study");
			EntityBuilder.addValue(ts, "date", new Date(2011,2,14));
			EntityBuilder.addValue(ts, "start_time", new Time(8,15));
			EntityBuilder.addValue(ts, "end_time", new Time(15,29));
		}
		{
			Entity ts = EntityBuilder.addEntity(cluster, "time_study");
			EntityBuilder.addValue(ts, "date", new Date(2011,2,15));
			EntityBuilder.addValue(ts, "start_time", new Time(8,32));
			EntityBuilder.addValue(ts, "end_time", new Time(11,20));
		}
		{
			Entity plot = EntityBuilder.addEntity(cluster, "plot");
			EntityBuilder.addValue(plot, "no", new Code("1"));
			Entity tree1 = EntityBuilder.addEntity(plot, "tree");
			EntityBuilder.addValue(tree1, "tree_no", 1);
			EntityBuilder.addValue(tree1, "dbh", 54.2);
			EntityBuilder.addValue(tree1, "total_height", 2.0);
//			EntityBuilder.addValue(tree1, "bole_height", (Double) null).setMetadata(new CollectAttributeMetadata('*',null,"No value specified"));
			RealAttribute boleHeight = EntityBuilder.addValue(tree1, "bole_height", (Double) null);
			boleHeight.getField(0).setSymbol('*');
			boleHeight.getField(0).setRemarks("No value specified");
			Entity tree2 = EntityBuilder.addEntity(plot, "tree");
			EntityBuilder.addValue(tree2, "tree_no", 2);
			EntityBuilder.addValue(tree2, "dbh", 82.8);
			EntityBuilder.addValue(tree2, "total_height", 3.0);
		}
		{
			Entity plot = EntityBuilder.addEntity(cluster, "plot");
			EntityBuilder.addValue(plot, "no", new Code("2"));
			Entity tree1 = EntityBuilder.addEntity(plot, "tree");
			EntityBuilder.addValue(tree1, "tree_no", 1);
			EntityBuilder.addValue(tree1, "dbh", 34.2);
			EntityBuilder.addValue(tree1, "total_height", 2.0);
			Entity tree2 = EntityBuilder.addEntity(plot, "tree");
			EntityBuilder.addValue(tree2, "tree_no", 2);
			EntityBuilder.addValue(tree2, "dbh", 85.8);
			EntityBuilder.addValue(tree2, "total_height", 4.0);
		}
	}

	public static void main(String[] args) throws Exception {
		CollectSurvey survey = loadSurvey();
		RelationalSchemaGenerator rsg = new RelationalSchemaGenerator();
		RelationalSchema rs = rsg.generateSchema(survey);
		List<Table<?>> tables = rs.getTables();
		// Debug
		for (Table<?> table : tables) {
			DataTable t = (DataTable) table;
			t.printDebug(System.out);
		}
		
		CollectRecord record = createTestRecord(survey, "123_456");
		Dataset data = rs.getData(record);
		data.printDebug(System.out);
	}
}
