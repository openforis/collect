package org.openforis.collect.designer.form;

import org.openforis.collect.metamodel.ui.UITab;

/**
 * 
 * @author S. Ricci
 *
 */
public class TabFormObject extends SurveyObjectFormObject<UITab> {
	
	private String label;
	
	private String defaultLabel;
	
	TabFormObject() {
	}
	
	public static TabFormObject newInstance() {
		TabFormObject formObject = new TabFormObject();
		return formObject;
	}
	
	@Override
	public void loadFrom(UITab source, String language) {
		label = source.getLabel(language);
		defaultLabel = source.getLabel();
	}
	
	@Override
	public void saveTo(UITab dest, String languageCode) {
		dest.setLabel(languageCode, label);
	}

	@Override
	protected void reset() {
		//TODO
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getDefaultLabel() {
		return defaultLabel;
	}
	
}
