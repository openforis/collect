/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.CodeListLevel;

/**
 * @author S. Ricci
 *
 */
public class CodeListLevelProxy implements ProxyBase {

	private transient CodeListLevel codeListLevel;

	public CodeListLevelProxy(CodeListLevel codeListLevel) {
		super();
		this.codeListLevel = codeListLevel;
	}

	public static List<CodeListLevelProxy> fromList(List<CodeListLevel> list) {
		List<CodeListLevelProxy> proxies = new ArrayList<CodeListLevelProxy>();
		if (list != null) {
			for (CodeListLevel v : list) {
				proxies.add(new CodeListLevelProxy(v));
			}
		}
		return proxies;
	}
	
	public List<LanguageSpecificTextProxy> getLabels() {
		return LanguageSpecificTextProxy.fromList(codeListLevel.getLabels());
	}

	public List<LanguageSpecificTextProxy> getDescriptions() {
		return LanguageSpecificTextProxy.fromList(codeListLevel.getDescriptions());
	}

	public String getName() {
		return codeListLevel.getName();
	};
	
	
	
}
