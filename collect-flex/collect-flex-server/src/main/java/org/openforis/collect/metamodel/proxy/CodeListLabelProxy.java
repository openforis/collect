/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.metamodel.CodeListLabel;

/**
 * @author S. Ricci
 * 
 */
public class CodeListLabelProxy implements Proxy {

	private transient CodeListLabel codeListLabel;

	public enum Type {
		ITEM, LIST
	}

	public CodeListLabelProxy(CodeListLabel codeListLabel) {
		super();
		this.codeListLabel = codeListLabel;
	}

	static List<CodeListLabelProxy> fromList(List<CodeListLabel> list) {
		List<CodeListLabelProxy> proxies = new ArrayList<CodeListLabelProxy>();
		if (list != null) {
			for (CodeListLabel v : list) {
				proxies.add(new CodeListLabelProxy(v));
			}
		}
		return proxies;
	}

	@ExternalizedProperty
	public Type getType() {
		if (codeListLabel.getType() != null) {
			return Type.valueOf(codeListLabel.getType().toString());
		} else {
			return null;
		}
	}

	@ExternalizedProperty
	public String getLanguage() {
		return codeListLabel.getLanguage();
	}

	@ExternalizedProperty
	public String getText() {
		return codeListLabel.getText();
	}

}
