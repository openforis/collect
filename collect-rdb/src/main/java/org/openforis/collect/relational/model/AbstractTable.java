package org.openforis.collect.relational.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.openforis.collect.relational.CollectRdbException;

/**
 * 
 * @author G. Miceli
 *
 */
abstract class AbstractTable<T> implements Table<T>  {

	private String prefix;
	private String baseName;
	private LinkedHashMap<String, Column<?>> columns;
	private PrimaryKeyConstraint primaryKeyConstraint;
//	private List<UniquenessConstraint> uniquenessConstraints;
	private List<ReferentialConstraint> referentialConstraints;
	
	AbstractTable(String prefix, String baseName) {
		this.prefix = prefix;
		this.baseName = baseName;
		this.columns = new LinkedHashMap<String, Column<?>>();
//		this.uniquenessConstraints = new ArrayList<UniquenessConstraint>();
		this.referentialConstraints = new ArrayList<ReferentialConstraint>();
	}
	
	@Override
	public String getPrefix() {
		return prefix;
	}
	
	@Override
	public String getBaseName() {
		return baseName;
	}

	@Override
	public String getName() {
		return prefix+baseName;
	}
	
	/**
	 * Adds a column or replaces existing column with same name
	 * @param column
	 * @throws CollectRdbException
	 */
	void addColumn(Column<?> column) {
		String columnName = column.getName();
		columns.put(columnName, column);
	}
	
	void setPrimaryKeyConstraint(PrimaryKeyConstraint primaryKeyConstraint) {
		this.primaryKeyConstraint = primaryKeyConstraint;
	}
	
//	void addConstraint(UniquenessConstraint constraint) {
//		uniquenessConstraints.add(constraint);
//	}
	
	void addConstraint(ReferentialConstraint constraint) {
		referentialConstraints.add(constraint);
	}
	
	@Override
	public List<Column<?>> getColumns() {
		ArrayList<Column<?>> columnList = new ArrayList<Column<?>>(columns.values());
		return Collections.unmodifiableList(columnList);
	}
	
	@Override
	public PrimaryKeyConstraint getPrimaryKeyConstraint() {
		return primaryKeyConstraint;
	}
//	@Override
//	public List<UniquenessConstraint> getUniquenessConstraints() {
//		return Collections.unmodifiableList(uniquenessConstraints);
//	}
	
	@Override
	public List<ReferentialConstraint> getReferentialContraints() {
		return Collections.unmodifiableList(referentialConstraints);
	}
	

	public boolean containsColumn(String name) {
		return columns.containsKey(name);
	}
}