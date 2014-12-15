package org.openforis.collect.metamodel.ui;

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
@Deprecated
public class UITabDefinition extends UITabSet {

	private static final long serialVersionUID = 1L;
	
	public UITab getDescendantTab(String name) {
		Stack<UITabSet> stack = new Stack<UITabSet>();
		while ( ! stack.isEmpty() ) {
			UITabSet group = stack.pop();
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
