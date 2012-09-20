package org.openforis.collect.model.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import org.openforis.idm.metamodel.xml.internal.XmlParent;
import org.openforis.idm.util.CollectionUtil;

/**
 * 
 * @author S. Ricci
 *
 */
@XmlTransient
public abstract class UITabsGroup implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@XmlAttribute(name = "name")
	protected String name;

	@XmlElementWrapper(name = "tabs")
	@XmlElement(name = "tab", type = UITab.class)
	protected List<UITab> tabs;

	@XmlParent
	@XmlTransient
	protected UITabsGroup parent;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public List<UITab> getTabs() {
		return CollectionUtil.unmodifiableList(tabs);
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
		tab.setParent(this);
	}
	
	public void setTab(int index, UITab tab) {
		tabs.set(index, tab);
	}
	
	public void removeTab(UITab tab) {
		tabs.remove(tab);
	}

	public UITab updateTab(String tabName, String newName, String newLabel) {
		UITab oldTab = getTab(tabName);
		oldTab.setName(newName);
		oldTab.setLabel(newLabel);
		return oldTab;
	}
	
	public UITabDefinition getTabDefinition() {
		if ( parent != null ) {
			return parent.getTabDefinition();
		} else if ( this instanceof UITabDefinition ) {
			return (UITabDefinition) this;
		} else {
			throw new IllegalStateException("Invalid tabs hierarchy: tab definition expected as root element");
		}
	}

	/**
	 * Returns the depth of the tab group.
	 * 
	 * @return 0 if this is a tab definition (root), > 0 otherwise
	 */
	public int getDepth() {
		int result = 0;
		UITabsGroup prnt = getParent();
		while ( prnt != null ) {
			result ++;
		}
		return result;
	}
	
	public UITabsGroup getParent() {
		return parent;
	}

	protected void setParent(UITabsGroup parent) {
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
		UITabsGroup other = (UITabsGroup) obj;
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
