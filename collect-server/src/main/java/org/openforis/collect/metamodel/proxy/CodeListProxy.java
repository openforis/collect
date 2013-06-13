/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.CodeList;

/**
 * @author M. Togna
 * @author S. Ricci
 */
public class CodeListProxy extends VersionableSurveyObjectProxy {

	private transient CodeList codeList;

	public enum CodeScope {
		SCHEME, LOCAL
	}

	public CodeListProxy(CodeList codeList) {
		super(codeList);
		this.codeList = codeList;
	}

	static List<CodeListProxy> fromList(List<CodeList> list) {
		List<CodeListProxy> proxies = new ArrayList<CodeListProxy>();
		if (list != null) {
			for (CodeList v : list) {
				proxies.add(new CodeListProxy(v));
			}
		}
		return proxies;
	}

	@ExternalizedProperty
	public String getName() {
		return codeList.getName();
	}

	@ExternalizedProperty
	public List<CodeListLabelProxy> getLabels() {
		return CodeListLabelProxy.fromList(codeList.getLabels());
	}

	@ExternalizedProperty
	public List<LanguageSpecificTextProxy> getDescriptions() {
		return LanguageSpecificTextProxy.fromList(codeList.getDescriptions());
	}

	@ExternalizedProperty
	public List<CodeListLevelProxy> getHierarchy() {
		return CodeListLevelProxy.fromList(codeList.getHierarchy());
	}

	@ExternalizedProperty
	public CodeScope getCodeScope() {
		if (codeList.getCodeScope() != null) {
			return CodeScope.valueOf(codeList.getCodeScope().toString());
		} else {
			return null;
		}
	}

}
