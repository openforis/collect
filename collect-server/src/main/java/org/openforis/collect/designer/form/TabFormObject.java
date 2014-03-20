package org.openforis.collect.designer.form;

import org.openforis.collect.metamodel.ui.UITab;

/**
 * 
 * @author S. Ricci
 *
 */
public class TabFormObject extends SurveyObjectFormObject<UITab> {
	
	private String label;
	
	TabFormObject() {
	}
	
	public static TabFormObject newInstance() {
		TabFormObject formObject = new TabFormObject();
		return formObject;
	}
	
	@Override
	public void loadFrom(UITab source, String language) {
		label = source.getLabel(language);
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
	
}
