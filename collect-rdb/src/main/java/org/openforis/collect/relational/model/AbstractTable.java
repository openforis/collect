package org.openforis.collect.relational.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.openforis.collect.relational.CollectRdbException;
import org.openforis.idm.metamodel.EntityDefinition;

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
	
	@Override
	public Column<?> getColumn(String name) {
		Column<?> column = columns.get(name);
		if ( column == null ) {
			throw new IllegalArgumentException("Column '" + name + "' not found in table '" + getName() + "'");
		} else {
			return column;
		}
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
	
	public List<ReferentialConstraint> getReferentialConstraintsByColumn(Column<?> column) {
		List<ReferentialConstraint> result = new ArrayList<ReferentialConstraint>();
		for (ReferentialConstraint constraint : referentialConstraints) {
			for (Column<?> constraingColumn : constraint.getColumns()) {
				if (constraingColumn.getName().equals(column.getName())) {
					result.add(constraint);
					break;
				}
			}
		}
		return result;
	}
	
	public EntityDefinition getReferencedEntityDefinition(DataAncestorFKColumn fkColumn) {
		DataTable referencedTable = getReferencedTable(fkColumn);
		EntityDefinition referencedEntityDef = (EntityDefinition) referencedTable.getNodeDefinition();
		return referencedEntityDef;
	}
	
	public DataTable getReferencedTable(Column<?> fkColumn) {
		List<ReferentialConstraint> constraints = getReferentialConstraintsByColumn(fkColumn);
		for (ReferentialConstraint constraint : constraints) {
			UniquenessConstraint referencedKey = constraint.getReferencedKey();
			if (referencedKey instanceof PrimaryKeyConstraint) {
				DataTable referencedTable = (DataTable) referencedKey.getTable();
				return referencedTable;
			}
		}
		throw new IllegalArgumentException(String.format("Referenced table not found for column %s in table %s", 
				fkColumn.getName(), getName()));
	}
	
	public boolean containsColumn(String name) {
		return columns.containsKey(name);
	}
	
	@Override
	public String toString() {
		return getName();
	}
}