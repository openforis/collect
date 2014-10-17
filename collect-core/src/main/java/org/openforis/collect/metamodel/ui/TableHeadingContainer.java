package org.openforis.collect.metamodel.ui;

import java.util.List;

/**
 * 
 * @author S. Ricci
 *
 */
public interface TableHeadingContainer {

	public List<TableHeadingComponent> getHeadingComponents();
	
	public void addHeadingComponent(TableHeadingComponent component);
	
	public void removeHeadingComponent(TableHeadingComponent component);

	public Column createColumn();
	
	public Column createColumn(int id);
	
	public ColumnGroup createColumnGroup();
	
	public ColumnGroup createColumnGroup(int id);
}
