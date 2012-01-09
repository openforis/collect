/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.CodeListLabel;

/**
 * @author S. Ricci
 *
 */
public class CodeListLabelProxy implements ProxyBase {

	private transient CodeListLabel codeListLabel;

	public enum TypeProxy { ITEM, LIST }
	
	public CodeListLabelProxy(CodeListLabel codeListLabel) {
		super();
		this.codeListLabel = codeListLabel;
	}

	public static List<CodeListLabelProxy> fromList(List<CodeListLabel> list) {
		List<CodeListLabelProxy> proxies = new ArrayList<CodeListLabelProxy>();
		if (list != null) {
			for (CodeListLabel v : list) {
				proxies.add(new CodeListLabelProxy(v));
			}
		}
		return proxies;
	}
	
	public TypeProxy getType() {
		switch(codeListLabel.getType()) {
			case ITEM:
				return TypeProxy.ITEM;
			case LIST:
				return TypeProxy.LIST;
			default:
				return null;
		}
	}

	public String getLanguage() {
		return codeListLabel.getLanguage();
	}

	public String getText() {
		return codeListLabel.getText();
	}
	
	
	
}
