/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.CodeListItem;

/**
 * @author S. Ricci
 *
 */
public class CodeListItemProxy implements ProxyBase {

	private transient CodeListItem codeListItem;

	public CodeListItemProxy(CodeListItem codeListItem) {
		super();
		this.codeListItem = codeListItem;
	}

	public static List<CodeListItemProxy> fromList(List<CodeListItem> list) {
		List<CodeListItemProxy> proxies = new ArrayList<CodeListItemProxy>();
		if (list != null) {
			for (CodeListItem v : list) {
				proxies.add(new CodeListItemProxy(v));
			}
		}
		return proxies;
	}
	
	public String getSinceVersionName() {
		return codeListItem.getSinceVersionName();
	}

	public String getDeprecatedVersionName() {
		return codeListItem.getDeprecatedVersionName();
	}

	public ModelVersionProxy getSinceVersion() {
		return new ModelVersionProxy(codeListItem.getSinceVersion());
	}

	public ModelVersionProxy getDeprecatedVersion() {
		return new ModelVersionProxy(codeListItem.getDeprecatedVersion());
	}

	public boolean isQualifiable() {
		return codeListItem.isQualifiable();
	}

	public String getCode() {
		return codeListItem.getCode();
	}

	public List<LanguageSpecificTextProxy> getLabels() {
		return LanguageSpecificTextProxy.fromList(codeListItem.getLabels());
	}

	public List<LanguageSpecificTextProxy> getDescriptions() {
		return LanguageSpecificTextProxy.fromList(codeListItem.getDescriptions());
	}

	public List<CodeListItemProxy> getChildItems() {
		return CodeListItemProxy.fromList(codeListItem.getChildItems());
	}

	
}
