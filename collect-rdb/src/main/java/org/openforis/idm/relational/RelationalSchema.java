package org.openforis.idm.relational;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.DefaultSurveyContext;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.metamodel.xml.SurveyIdmlBinder;
import org.openforis.idm.model.Record;
import org.openforis.idm.transform.Transformation;

/**
 * 
 * @author G. Miceli
 *
 */
public final class RelationalSchema {

	private Survey survey;
	private LinkedHashMap<String, Table> tables;
	private HashMap<Integer, Table> dataTables;
	private Transformation transformation;

	public RelationalSchema(Survey survey) throws SchemaGenerationException {
		this.survey = survey;
		generateSchema();
	}

	public Survey getSurvey() {
		return survey;
	}
	
	public List<Table> getTables() {
		List<Table> tableList = new ArrayList<Table>(tables.values());
		return Collections.unmodifiableList(tableList);
	}

	public Dataset getReferenceData() {
		return null;
	}
	
	public Dataset toData(Record record) {
		return null;
	}
	
	private void generateSchema() throws SchemaGenerationException {
		tables = new LinkedHashMap<String, Table>();
		createCodeListTables();
		transformation = new Transformation();
		createDataTables();
	}

	private void createCodeListTables() throws SchemaGenerationException {
		// TODO Auto-generated method stub
		
	}

	private void createDataTables() throws SchemaGenerationException {
		dataTables = new HashMap<Integer, Table>();
		Schema schema = survey.getSchema();
		List<EntityDefinition> roots = schema.getRootEntityDefinitions();
		for (EntityDefinition root : roots) {
			createDataTables(root);
		}
		for (Table table : tables.values()) {
			DataTable t = (DataTable) table;
			t.printDebug(System.out);
		}
	}

	private void createDataTables(NodeDefinition defn) throws SchemaGenerationException {
		if ( defn instanceof EntityDefinition ) {
			EntityDefinition entityDefn = (EntityDefinition) defn;
			for (NodeDefinition child : entityDefn.getChildDefinitions()) {
				createDataTables(child);
			}			
		}
		if ( defn.isMultiple() ) {
			DataTable table = new DataTable(defn);
			String name = table.getName();
			if ( tables.containsKey(name) ) {
				throw new SchemaGenerationException("Duplicate table name '"+name+"' for "+defn.getPath());
			}
			tables.put(name, table);
			dataTables.put(defn.getId(), table);
		}
//		Pivot pivot = Pivot.fromNodeDefinition(defn);
//		
//		if ( defn instanceof )
//		for (NodeDefinition child : defn.getChildDefinitions()) {
//			if ( child instanceof AttributeDefinition && !child.isMultiple() ) {
//				// Single attributes become columns
//			} else {
//				// All other multiple nodes become tables
//				createDataTables();
//			}
//		}
//			String name = defn.getAnnotation(TABLE_NAME_QNAME);
//			if ( name == null ) {
//				name = defn.getName();
//				if ( tablePrefix != null ) {
//					name = tablePrefix + "_" + name;
//				}
//			}
//			Table table = createDataTable(defn, parentTable, tablePrefix);
	}
	
//	private void createDataTable(NodeDefinition defn) {
//		System.out.println("Table: "+defn.getPath());
//		if ( defn instanceof EntityDefinition ) {
//			addDataColumns((EntityDefinition) defn);
//		} else if ( defn instanceof AttributeDefinition) ) {
//			addDataColumns((AttributeDefinition) defn);
//		}
//	}
	
	//
//	private void createDataTables(NodeDefinition defn, Table parentTable) {
//		if ( defn.isMultiple() ) {
//			String name = getTableName(defn);
//			Table table = new Table(name);
//		}
//		
//	}


//	private void createChildDataTables(NodeDefinition defn) throws SchemaGenerationException {
//		if ( defn instanceof EntityDefinition ) {
//			EntityDefinition entityDefn = (EntityDefinition) defn;
//			List<NodeDefinition> children = entityDefn.getChildDefinitions();
//			for (NodeDefinition child : children) {
//				createDataTables(defn);
//			}
//		}
//	}
	
	private void addDataColumns(NodeDefinition defn) {
		// TODO Auto-generated method stub
		
	}

	private Table createDataTable(NodeDefinition defn, Table parentTable, String tablePrefix) {
		// TODO Auto-generated method stub
		return null;
	}

	private static Survey loadSurvey() throws IdmlParseException, FileNotFoundException {
//		InputStream is = ClassLoader.getSystemResourceAsStream("test.idm.xml");
		InputStream is = new FileInputStream("/home/gino/workspace/of/idm/idm-test/src/main/resources/test.idm.xml");
//		InputStream is = new FileInputStream("/home/gino/workspace/faofin/tz/naforma-idm/tanzania-naforma.idm.xml");
		SurveyContext ctx = new DefaultSurveyContext();
		SurveyIdmlBinder binder = new SurveyIdmlBinder(ctx);
		return binder.unmarshal(is);
	}

	public static void main(String[] args) throws Exception {
		Survey survey = loadSurvey();
		RelationalSchema rs = new RelationalSchema(survey);
	}
}