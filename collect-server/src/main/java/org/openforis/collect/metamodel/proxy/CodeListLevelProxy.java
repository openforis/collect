/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.metamodel.CodeListLevel;

/**
 * @author S. Ricci
 * 
 */
public class CodeListLevelProxy implements Proxy {

	private transient CodeListLevel codeListLevel;

	public CodeListLevelProxy(CodeListLevel codeListLevel) {
		super();
		this.codeListLevel = codeListLevel;
	}

	static List<CodeListLevelProxy> fromList(List<CodeListLevel> list) {
		List<CodeListLevelProxy> proxies = new ArrayList<CodeListLevelProxy>();
		if (list != null) {
			for (CodeListLevel v : list) {
				proxies.add(new CodeListLevelProxy(v));
			}
		}
		return proxies;
	}

	@ExternalizedProperty
	public List<LanguageSpecificTextProxy> getLabels() {
		return LanguageSpecificTextProxy.fromList(codeListLevel.getLabels());
	}

	@ExternalizedProperty
	public List<LanguageSpecificTextProxy> getDescriptions() {
		return LanguageSpecificTextProxy.fromList(codeListLevel.getDescriptions());
	}

	@ExternalizedProperty
	public String getName() {
		return codeListLevel.getName();
	};

}
