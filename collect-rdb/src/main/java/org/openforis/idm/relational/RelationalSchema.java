package org.openforis.idm.relational;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.namespace.QName;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Record;

/**
 * 
 * @author G. Miceli
 *
 */
public final class RelationalSchema {

	private Survey survey;
	private LinkedHashMap<String, Table> tables;

	public RelationalSchema(Survey survey) throws SchemaGenerationException {
		this.survey = survey;
		updateSchema();
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
	
	private void updateSchema() throws SchemaGenerationException {
		tables = new LinkedHashMap<String, Table>();
		createCodeListTables();
		createDataTables();
	}

	private void createCodeListTables() throws SchemaGenerationException {
		// TODO Auto-generated method stub
		
	}

	private void createDataTables() throws SchemaGenerationException {
		Schema schema = survey.getSchema();
		List<EntityDefinition> roots = schema.getRootEntityDefinitions();
		for (EntityDefinition root : roots) {
			createDataTables(root, null, null);
		}
	}

	private void createDataTables(NodeDefinition defn, Table parentTable, String tablePrefix) {
		if ( defn.isMultiple() ) {
			String name = defn.getAnnotation(TABLE_NAME_QNAME);
			if ( name == null ) {
				name = defn.getName();
				if ( tablePrefix != null ) {
					name = tablePrefix + "_" + name;
				}
			}
			Table table = createDataTable(defn, parentTable, tablePrefix);
			
		}
	}
	
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
	
	private Table createDataTable(NodeDefinition defn, Table parentTable, String tablePrefix) {
		// TODO Auto-generated method stub
		return null;
	}

}