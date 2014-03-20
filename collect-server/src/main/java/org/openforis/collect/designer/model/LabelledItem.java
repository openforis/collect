package org.openforis.collect.designer.model;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus
 *
 */
public class LabelledItem {
	String code;
	String label;

	public LabelledItem(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
}