package org.openforis.idm.relational;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 
 * @author G. Miceli
 *
 */
public class Table  {

	private String name;
	private LinkedHashMap<String, Column> columns;
	private List<UniquenessConstraint> uniquenessConstraints;
	private List<ReferentialConstraint> referentialConstraints;
	
	Table(String name) {
		this.name = name;
		this.columns = new LinkedHashMap<String, Column>();
		this.uniquenessConstraints = new ArrayList<UniquenessConstraint>();
		this.referentialConstraints = new ArrayList<ReferentialConstraint>();
	}
	
	public String getName() {
		return name;
	}

	void addColumn(Column column) throws SchemaGenerationException {
		String columnName = column.getName();
		if ( columns.containsKey(columnName) ) {
			throw new SchemaGenerationException("Duplicate column '"+columnName+"' in table '"+name+"'");
		}
		columns.put(name, column);
	}
	
	void addConstraint(UniquenessConstraint constraint) {
		uniquenessConstraints.add(constraint);
	}
	
	void addConstraint(ReferentialConstraint constraint) {
		referentialConstraints.add(constraint);
	}
	
	public List<Column> getColumns() {
		ArrayList<Column> columnList = new ArrayList<Column>(columns.values());
		return Collections.unmodifiableList(columnList);
	}
	
	public List<UniquenessConstraint> getUniquenessConstraints() {
		return Collections.unmodifiableList(uniquenessConstraints);
	}
	
	public List<ReferentialConstraint> getReferentialContraints() {
		return Collections.unmodifiableList(referentialConstraints);
	}
}