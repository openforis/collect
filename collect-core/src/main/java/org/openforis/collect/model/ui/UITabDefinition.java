package org.openforis.collect.model.ui;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author S. Ricci
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "rootEntity", "tabs" })
public class UITabDefinition implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlAttribute(name = "rootEntity")
	private String rootEntity;

	@XmlElementWrapper(name = "tabs")
	@XmlElement(name = "tab", type = UITab.class)
	private List<UITab> tabs;

	public String getRootEntity() {
		return rootEntity;
	}

	public List<UITab> getTabs() {
		return tabs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rootEntity == null) ? 0 : rootEntity.hashCode());
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
		UITabDefinition other = (UITabDefinition) obj;
		if (rootEntity == null) {
			if (other.rootEntity != null)
				return false;
		} else if (!rootEntity.equals(other.rootEntity))
			return false;
		if (tabs == null) {
			if (other.tabs != null)
				return false;
		} else if (!tabs.equals(other.tabs))
			return false;
		return true;
	}
	
}
