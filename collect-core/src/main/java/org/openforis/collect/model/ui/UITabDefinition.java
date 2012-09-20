package org.openforis.collect.model.ui;

import java.util.List;
import java.util.Stack;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author S. Ricci
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "name", "tabs" })
public class UITabDefinition extends UITabsGroup {

	private static final long serialVersionUID = 1L;
	
	public UITab getDescendantTab(String name) {
		Stack<UITabsGroup> stack = new Stack<UITabsGroup>();
		while ( ! stack.isEmpty() ) {
			UITabsGroup group = stack.pop();
			List<UITab> tabs = group.getTabs();
			for (UITab uiTab : tabs) {
				String tabName = uiTab.getName();
				if ( name.equals(tabName)) {
					return uiTab;
				}
			}
			stack.addAll(tabs);
		}
		return null;
	}
	

}
