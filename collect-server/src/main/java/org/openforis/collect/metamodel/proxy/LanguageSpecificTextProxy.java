/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.metamodel.LanguageSpecificText;

/**
 * @author M. Togna
 * 
 */
public class LanguageSpecificTextProxy implements Proxy {

	private transient LanguageSpecificText languageSpecificText;

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
		this.languageSpecificText = text;
	}

	@ExternalizedProperty
	public String getLanguage() {
		return languageSpecificText.getLanguage();
	}

	@ExternalizedProperty
	public String getText() {
		return languageSpecificText.getText();
	}

}
