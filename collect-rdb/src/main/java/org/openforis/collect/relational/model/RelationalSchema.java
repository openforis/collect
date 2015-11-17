package org.openforis.collect.relational.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.openforis.collect.relational.CollectRdbException;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Survey;

/**
 * 
 * @author G. Miceli
 *
 */
public final class RelationalSchema {

	private Survey survey;
	private String name;
	private Map<String, Table<?>> tablesByName;
	private Map<Integer, DataTable> dataTableByDefinitionId;
	private Map<CodeListTableKey, CodeTable> codeListTables;
	private Map<String, DataTable> rootDataTables;
	
	RelationalSchema(Survey survey, String name) throws CollectRdbException {
		this.survey = survey;
		this.name = name;
		this.tablesByName = new LinkedHashMap<String, Table<?>>();
		this.codeListTables = new LinkedHashMap<CodeListTableKey, CodeTable>();
		this.rootDataTables = new HashMap<String, DataTable>();
		this.dataTableByDefinitionId = new HashMap<Integer, DataTable>();
	}

	public Survey getSurvey() {
		return survey;
	}
	
	public String getName() {
		return name;
	}
	
	public List<Table<?>> getTables() {
		List<Table<?>> tableList = new ArrayList<Table<?>>(tablesByName.values());
		return Collections.unmodifiableList(tableList);
	}
	
	/**
	 * Returns a list of data tables hierarchically sorted (BFS)
	 */
	public List<DataTable> getDataTables() {
		List<DataTable> result = new ArrayList<DataTable>();
		for (DataTable dataTable : getRootDataTables()) {
			result.add(dataTable);
			int rootDefId = dataTable.getNodeDefinition().getId();
			result.addAll(getDescendantTablesForDefinition(rootDefId));
		}
		return result;
	}
	
	public List<? extends DataTable> getDescendantTablesForDefinition(
			int definitionId) {
		List<DataTable> result = new ArrayList<DataTable>();
		Queue<DataTable> queue = new LinkedList<DataTable>();
		
		queue.add(getDataTableByDefinitionId(definitionId));
		
		while(! queue.isEmpty()) {
			DataTable table = queue.poll();
			result.add(table);
			queue.addAll(table.getChildTables());
		}
		//do not include the actual data table
		return result.subList(1, result.size());
	}

	public Collection<DataTable> getRootDataTables() {
		return rootDataTables.values();
	}
	
	public DataTable getRootDataTable(String rootEntityName) {
		return rootDataTables.get(rootEntityName);
	}
	
	public Table<?> getTable(String name) {
		Table<?> table = tablesByName.get(name);
		if ( table == null ) {
			throw new IllegalArgumentException("Table not found: " + name);
		} else {
			return table;
		}
	}
	
	public List<CodeTable> getCodeListTables() {
		List<CodeTable> tableList = new ArrayList<CodeTable>(codeListTables.values());
		return Collections.unmodifiableList(tableList);
	}
	
	public CodeTable getCodeListTable(CodeAttributeDefinition attrDef) {
		return getCodeListTable(attrDef.getList(), attrDef.getListLevelIndex());
	}
	
	public CodeTable getCodeListTable(CodeList list, Integer levelIdx) {
		CodeListTableKey key = new CodeListTableKey(list.getId(), levelIdx);
		return codeListTables.get(key);
	}

	public boolean containsTable(String name) {
		return tablesByName.containsKey(name);
	}

	/**
	 * Adds a table to the schema.  If table with the same name 
	 * already exists it will be replaced.
	 * @param table
	 */
	void addTable(Table<?> table) {
		String name = table.getName();
		tablesByName.put(name, table);
		if ( table instanceof DataTable ) {
			DataTable dataTable = (DataTable) table;
			NodeDefinition defn = dataTable.getNodeDefinition();
			dataTableByDefinitionId.put(defn.getId(), dataTable);
			if ( dataTable.getParent() == null ) {
				rootDataTables.put(defn.getName(), dataTable);
			}
		} else if ( table instanceof CodeTable ) {
			CodeTable codeListTable = (CodeTable) table;
			CodeList codeList = codeListTable.getCodeList();
			CodeListTableKey key = new CodeListTableKey(codeList.getId(), codeListTable.getLevelIdx());
			codeListTables.put(key, codeListTable);
		}
	}

	void assignAncestorTable(EntityDefinition entityDefn) {
		int nodeId = entityDefn.getId();
		while( ! entityDefn.isMultiple() ) {
				entityDefn = entityDefn.getParentEntityDefinition();
		}
		dataTableByDefinitionId.put( nodeId, dataTableByDefinitionId.get(entityDefn.getId()) );
	}

	public DataTable getDataTable(NodeDefinition nodeDefinition) {
		int id = nodeDefinition.getId();
		return getDataTableByDefinitionId(id);
	}
	
	public DataTable getDataTableByDefinitionId(int id) {
		return dataTableByDefinitionId.get(id);
	}
	
	private static class CodeListTableKey {
		private int listId;
		private Integer levelIdx;
		
		CodeListTableKey(int listId, Integer levelIdx) {
			this.listId = listId;
			this.levelIdx = levelIdx;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((levelIdx == null) ? 0 : levelIdx.hashCode());
			result = prime * result + listId;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CodeListTableKey other = (CodeListTableKey) obj;
			if (levelIdx == null) {
				if (other.levelIdx != null)
					return false;
			} else if (!levelIdx.equals(other.levelIdx))
				return false;
			if (listId != other.listId)
				return false;
			return true;
		}
		
	}

}