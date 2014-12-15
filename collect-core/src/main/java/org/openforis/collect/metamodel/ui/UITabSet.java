package org.openforis.collect.metamodel.ui;

import java.util.ArrayList;
import java.util.List;

import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.SurveyObject;

/**
 * 
 * @author S. Ricci
 *
 */
public class UITabSet extends SurveyObject {
	
	private static final long serialVersionUID = 1L;
	
	protected UIOptions uiOptions;
	protected String name;
	protected List<UITab> tabs;
	protected UITabSet parent;
	
	UITabSet(UIOptions uiOptions) {
		super(uiOptions.getSurvey());
		this.uiOptions = uiOptions;
	}
	
	public UIOptions getUIOptions() {
		return uiOptions;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public List<UITab> getTabs() {
		return CollectionUtils.unmodifiableList(tabs);
	}
	
	public UITab getTab(String name) {
		if ( tabs != null ) {
			for (UITab tab : tabs) {
				if ( tab.getName().equals(name) ) {
					return tab;
				}
			}
		}
		return null;
	}
	
	public void addTab(UITab tab) {
		if ( tabs == null ) {
			tabs = new ArrayList<UITab>();
		}
		tabs.add(tab);
		tab.parent = this;
	}
	
	public void setTab(int index, UITab tab) {
		tabs.set(index, tab);
	}
	
	public void removeTab(UITab tab) {
		tab.detatch();
		tabs.remove(tab);
	}
	
	public void detatch() {
	}

	public void moveTab(UITab tab, int newIndex) {
		CollectionUtils.shiftItem(tabs, tab, newIndex);
	}

	public UITab updateTab(String tabName, String newName, String newLabel, String language) {
		UITab tab = getTab(tabName);
		tab.setName(newName);
		tab.setLabel(language, newLabel);
		return tab;
	}
	
	public UITabSet getRootTabSet() {
		if ( parent == null ) {
			return this;
		} else {
			return parent.getRootTabSet();
		}
	}

	/**
	 * Returns the depth of the tab group.
	 * 
	 * @return 0 if this is a tab definition (root), > 0 otherwise
	 */
	public int getDepth() {
		int result = 0;
		UITabSet prnt = getParent();
		while ( prnt != null ) {
			result ++;
			prnt = prnt.getParent();
		}
		return result;
	}
	
	public UITabSet getParent() {
		return parent;
	}

	protected void setParent(UITabSet parent) {
		this.parent = parent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((tabs == null) ? 0 : tabs.hashCode());
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
		UITabSet other = (UITabSet) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (tabs == null) {
			if (other.tabs != null)
				return false;
		} else if (!tabs.equals(other.tabs))
			return false;
		return true;
	}

}
