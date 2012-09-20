package org.openforis.collect.model.ui;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author S. Ricci
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "name", "label", "tabs" })
public class UITab extends UITabsGroup {

	private static final long serialVersionUID = 1L;

	@XmlElement(name = "label")
	private String label;

	@XmlTransient
	private UITabDefinition tabDefinition;
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result
				+ ((tabDefinition == null) ? 0 : tabDefinition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		UITab other = (UITab) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (tabDefinition == null) {
			if (other.tabDefinition != null)
				return false;
		} else if (!tabDefinition.equals(other.tabDefinition))
			return false;
		return true;
	}

}
