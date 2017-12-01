package org.openforis.collect.metamodel.ui;

import java.util.List;

/**
 * 
 * @author S. Ricci
 *
 */
public interface UITableHeadingContainer {

	public List<UITableHeadingComponent> getHeadingComponents();
	
	public void addHeadingComponent(UITableHeadingComponent component);
	
	public void removeHeadingComponent(UITableHeadingComponent component);
	
	public int indexOf(UITableHeadingComponent component);

	public UIColumn createColumn();
	
	public UIColumn createColumn(int id);
	
	public UIColumnGroup createColumnGroup();
	
	public UIColumnGroup createColumnGroup(int id);
}
