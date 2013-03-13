package org.openforis.collect.metamodel.ui;

import java.util.Collections;
import java.util.List;

import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.LanguageSpecificTextMap;

/**
 * 
 * @author S. Ricci
 * 
 */
public class UITab extends UITabSet {

	public UITab() {
		super(null);
	}
	
	UITab(UIOptions uiOptions) {
		super(uiOptions);
	}

	private static final long serialVersionUID = 1L;

	private LanguageSpecificTextMap labels;

	public List<LanguageSpecificText> getLabels() {
		if ( labels == null ) {
			return Collections.emptyList();
		} else {
			return labels.values();
		}
	}
	
	public String getLabel(String language) {
		return labels == null ? null: labels.getText(language);
	}
	
	public void addLabel(LanguageSpecificText label) {
		if ( labels == null ) {
			labels = new LanguageSpecificTextMap();
		}
		labels.add(label);
	}

	public void setLabel(String language, String text) {
		if ( labels == null ) {
			labels = new LanguageSpecificTextMap();
		}
		labels.setText(language, text);
	}
	
	public void removeLabel(String language) {
		labels.remove(language);
	}
	
	public List<UITab> getSiblings() {
		UITabSet parent = getParent();
		return parent.getTabs();
	}
	
	public int getIndex() {
		List<UITab> siblings = getSiblings();
		int index = siblings.indexOf(this);
		return index;
	}

	@Override
	public void detatch() {
		super.detatch();
		uiOptions.removeTabAssociation(this);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((labels == null) ? 0 : labels.hashCode());
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
		if (labels == null) {
			if (other.labels != null)
				return false;
		} else if (!labels.equals(other.labels))
			return false;
		return true;
	}

}
