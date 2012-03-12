package org.openforis.collect.model;

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
	
}
