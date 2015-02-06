package org.openforis.collect.io.metadata.collectearth.balloon;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author A. Sanchez-Paus Diaz
 * @author S. Ricci
 *
 */
class CEComponent {

	private String htmlParameterName;
	private String name;
	private String label;
	private boolean multiple;
	List<CEComponent> relevanceSources = new ArrayList<CEComponent>();
	boolean hideWhenNotRelevant = false;

	public CEComponent(String htmlParameterName, String name, String label, boolean multiple) {
		super();
		this.htmlParameterName = htmlParameterName;
		this.name = name;
		this.label = label;
		this.multiple = multiple;
	}

	public String getHtmlParameterName() {
		return htmlParameterName;
	}
	
	public String getName() {
		return name;
	}

	public String getLabel() {
		return label;
	}

	public boolean isMultiple() {
		return multiple;
	}
}