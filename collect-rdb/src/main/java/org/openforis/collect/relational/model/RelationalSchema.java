package org.openforis.collect.relational.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.relational.CollectRdbException;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Record;

/**
 * 
 * @author G. Miceli
 *
 */
public final class RelationalSchema {

	private Survey survey;
	private String name;
	private LinkedHashMap<String, Table<?>> tables;
	private Map<String, DataTable> rootDataTables;
	
	RelationalSchema(Survey survey, String name) throws CollectRdbException {
		this.survey = survey;
		this.name = name;
		this.tables = new LinkedHashMap<String, Table<?>>();
		this.rootDataTables = new HashMap<String, DataTable>();
	}

	public Survey getSurvey() {
		return survey;
	}
	
	public String getName() {
		return name;
	}
	
	public List<Table<?>> getTables() {
		List<Table<?>> tableList = new ArrayList<Table<?>>(tables.values());
		return Collections.unmodifiableList(tableList);
	}

	public Dataset getReferenceData() {
		return null;
	}
	
	public boolean containsTable(String name) {
		return tables.containsKey(name);
	}

	/**
	 * Adds a table to the schema.  If table with the same name 
	 * already exists it will be replaced.
	 * @param table
	 */
	void addTable(Table<?> table) {
		String name = table.getName();
		tables.put(name, table);
		if ( table instanceof DataTable ) {
			DataTable dataTable = (DataTable) table;
			if ( dataTable.getParent() == null ) {
				NodeDefinition defn = dataTable.getNodeDefinition();
				rootDataTables.put(defn.getName(), dataTable);
			}
		}
	}

	public Dataset createDataset(Record record) {
		Entity root = record.getRootEntity();
		EntityDefinition rootDefn = root.getDefinition();
		String name = rootDefn.getName();
		DataTable dataTable = rootDataTables.get(name);
		if ( dataTable == null ) {
			throw new IllegalArgumentException("Invalid root entity "+name);
		}
		return dataTable.extractData(root);
	}
}