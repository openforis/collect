/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.LanguageSpecificText;

/**
 * @author M. Togna
 * 
 */
public class LanguageSpecificTextProxy implements ProxyBase{

	private transient LanguageSpecificText text;

	public static List<LanguageSpecificTextProxy> fromList(List<LanguageSpecificText> list) {
		List<LanguageSpecificTextProxy> proxies = new ArrayList<LanguageSpecificTextProxy>();
		if (list != null) {
			for (LanguageSpecificText text : list) {
				proxies.add(new LanguageSpecificTextProxy(text));
			}
		}
		return proxies;
	}

	public LanguageSpecificTextProxy(LanguageSpecificText text) {
		super();
		this.text = text;
	}

	@ExternalizedProperty
	public String getLanguage() {
		return text.getLanguage();
	}

	@ExternalizedProperty
	public String getText() {
		return text.getText();
	}

}
