/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.CodeList;

/**
 * @author M. Togna
 * @author S. Ricci
 */
public class CodeListProxy implements ProxyBase {

	private transient CodeList codeList;

	public enum CodeTypeProxy {
		NUMERIC, ALPHANUMERIC
	}

	public enum CodeScopeProxy {
		SCHEME, LOCAL
	}
	
	public CodeListProxy(CodeList codeList) {
		super();
		this.codeList = codeList;
	}

	public static List<CodeListProxy> fromList(List<CodeList> list) {
		List<CodeListProxy> proxies = new ArrayList<CodeListProxy>();
		if (list != null) {
			for (CodeList v : list) {
				proxies.add(new CodeListProxy(v));
			}
		}
		return proxies;
	}
	
	public String getSinceVersionName() {
		return codeList.getSinceVersionName();
	}

	public String getDeprecatedVersionName() {
		return codeList.getDeprecatedVersionName();
	}

	public ModelVersionProxy getSinceVersion() {
		if(codeList.getSinceVersion() != null) {
			return new ModelVersionProxy(codeList.getSinceVersion());
		} else return null;
	}

	public ModelVersionProxy getDeprecatedVersion() {
		if(codeList.getDeprecatedVersion() != null) {
			return new ModelVersionProxy(codeList.getDeprecatedVersion());
		} else return null;
	}

	public String getName() {
		return codeList.getName();
	}

	public List<CodeListLabelProxy> getLabels() {
		return CodeListLabelProxy.fromList(codeList.getLabels());
	}

	public List<LanguageSpecificTextProxy> getDescriptions() {
		return LanguageSpecificTextProxy.fromList(codeList.getDescriptions());
	}

	public List<CodeListLevelProxy> getHierarchy() {
		return CodeListLevelProxy.fromList(codeList.getHierarchy());
	}

	public List<CodeListItemProxy> getItems() {
		return CodeListItemProxy.fromList(codeList.getItems());
	}

	public CodeTypeProxy getCodeType() {
		switch(codeList.getCodeType()) {
			case ALPHANUMERIC:
				return CodeTypeProxy.ALPHANUMERIC;
			case NUMERIC:
				return CodeTypeProxy.NUMERIC;
			default:
				return null;
		}
	}

	public CodeScopeProxy getCodeScope() {
		switch(codeList.getCodeScope()) {
			case LOCAL:
				return CodeScopeProxy.LOCAL;
			case SCHEME:
				return CodeScopeProxy.SCHEME;
			default:
				return null;
		}
	}

	public boolean isAlphanumeric() {
		return codeList.isAlphanumeric();
	}

	public boolean isNumeric() {
		return codeList.isNumeric();
	}

	
}
