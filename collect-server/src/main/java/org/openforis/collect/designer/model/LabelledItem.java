package org.openforis.collect.designer.model;

import java.util.Collection;
import java.util.Comparator;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus
 *
 */
public class LabelledItem {
	private String code;
	private String label;

	public static LabelledItem getByCode(Collection<LabelledItem> items, String code) {
		for (LabelledItem item : items) {
			if ( item.getCode().equals(code) ) {
				return item;
			}
		}
		return null;
	}
	
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
	
	@Override
	public String toString() {
		return "LabelledItem [code=" + code + ", label=" + label + "]";
	}

	public static class LabelComparator implements Comparator<LabelledItem> {
		@Override
		public int compare(LabelledItem item1, LabelledItem item2) {
			return item1.getLabel().compareTo(item2.getLabel());
		}
	}

}