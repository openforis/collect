package org.openforis.collect.designer.model;

import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class NamedObject {

	private String nameLabelKey;
	
	public NamedObject(String nameLabelKey) {
		super();
		this.nameLabelKey = nameLabelKey;
	}

	public String getName() {
		return Labels.getLabel(nameLabelKey);
	}
	
	public void setLabelKey(String labelKey) {
		this.nameLabelKey = labelKey;
	}
	
}
