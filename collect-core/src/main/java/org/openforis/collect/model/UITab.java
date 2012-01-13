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
@XmlType(name = "", propOrder = { "name", "label", "tabs" })
public class UITab implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlAttribute(name = "name")
	private String name;

	@XmlElement(name = "label")
	private String label;

	@XmlElementWrapper(name = "tabs")
	@XmlElement(name = "tab", type = UITab.class)
	private List<UITab> tabs;

	public String getName() {
		return name;
	}

	public String getLabel() {
		return label;
	}

	public List<UITab> getTabs() {
		return tabs;
	}

}
