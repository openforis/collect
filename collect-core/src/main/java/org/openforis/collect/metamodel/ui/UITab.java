package org.openforis.collect.metamodel.ui;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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

	@Override
	public void detatch() {
		super.detatch();
		uiOptions.removeTabAssociation(this);
		parent = null;
	}

	public List<LanguageSpecificText> getLabels() {
		if ( labels == null ) {
			return Collections.emptyList();
		} else {
			return labels.values();
		}
	}
	
	public String getLabel() {
		return getLabel(getSurvey().getDefaultLanguage());
	}
	
	public String getLabel(String language) {
		return getLabel(language, null);
	}
	
	public String getLabel(String language, String defaultLanguage) {
		return labels == null ? null : labels.getText(language, defaultLanguage);
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
	
	public String getPath(String language) {
		return getPath(language, null);
	}
	
	public String getPath(String language, String nullValuesReplace) {
		StringBuilder sb = new StringBuilder();
		UITab currentTab = this;
		while ( currentTab != null ) {
			if ( currentTab != this ) {
				sb.insert(0, "/");
			}
			String label = currentTab.getLabel(language);
			if ( nullValuesReplace != null && StringUtils.isBlank(label) ) {
				sb.insert(0, nullValuesReplace);
			} else {
				sb.insert(0, label);
			}
			UITabSet parent = currentTab.getParent();
			if ( parent instanceof UITab ) {
				currentTab = (UITab) parent;
			} else {
				break;
			}
		}
		return sb.toString();
	}

	public boolean isDescendantOf(UITab parentTab) {
		UITabSet currentParent = this.parent;
		while ( currentParent != null ) {
			if ( currentParent instanceof UITab && currentParent == parentTab ) {
				return true;
			}
			currentParent = currentParent.parent;
		}
		return false;
	}
	
	public void traverse(TabVisitor visitor) {
		Deque<UITab> stack = new LinkedList<UITab>();
		stack.push(this);
		while (! stack.isEmpty()) {
			UITab tab = stack.pop();
			visitor.visit(tab);
			stack.addAll(tab.getTabs());
		}
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

	public interface TabVisitor {
		void visit(UITab tab);
	}
}
