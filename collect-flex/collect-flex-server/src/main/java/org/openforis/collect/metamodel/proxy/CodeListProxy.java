/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.metamodel.CodeList;

/**
 * @author M. Togna
 * @author S. Ricci
 */
public class CodeListProxy implements Proxy {

	private transient CodeList codeList;

	public enum CodeScope {
		SCHEME, LOCAL
	}

	public CodeListProxy(CodeList codeList) {
		super();
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
	public String getSinceVersionName() {
		return codeList.getSinceVersionName();
	}

	@ExternalizedProperty
	public String getDeprecatedVersionName() {
		return codeList.getDeprecatedVersionName();
	}

	@ExternalizedProperty
	public ModelVersionProxy getSinceVersion() {
		if (codeList.getSinceVersion() != null) {
			return new ModelVersionProxy(codeList.getSinceVersion());
		} else
			return null;
	}

	@ExternalizedProperty
	public ModelVersionProxy getDeprecatedVersion() {
		if (codeList.getDeprecatedVersion() != null) {
			return new ModelVersionProxy(codeList.getDeprecatedVersion());
		} else
			return null;
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
	public List<CodeListItemProxy> getItems() {
		return CodeListItemProxy.fromList(codeList.getItems());
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
