/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.metamodel.CodeListItem;

/**
 * @author S. Ricci
 *
 */
public class CodeListItemProxy implements Proxy {

	private transient CodeListItem codeListItem;

	public CodeListItemProxy(CodeListItem codeListItem) {
		super();
		this.codeListItem = codeListItem;
	}

	static List<CodeListItemProxy> fromList(List<CodeListItem> list) {
		List<CodeListItemProxy> proxies = new ArrayList<CodeListItemProxy>();
		if (list != null) {
			for (CodeListItem v : list) {
				proxies.add(new CodeListItemProxy(v));
			}
		}
		return proxies;
	}
	
	@ExternalizedProperty
	public String getSinceVersionName() {
		return codeListItem.getSinceVersionName();
	}

	@ExternalizedProperty
	public String getDeprecatedVersionName() {
		return codeListItem.getDeprecatedVersionName();
	}

	@ExternalizedProperty
	public ModelVersionProxy getSinceVersion() {
		if(codeListItem.getSinceVersion() != null) {
			return new ModelVersionProxy(codeListItem.getSinceVersion());
		} else {
			return null;
		}
	}

	@ExternalizedProperty
	public ModelVersionProxy getDeprecatedVersion() {
		if(codeListItem.getDeprecatedVersion() != null) {
			return new ModelVersionProxy(codeListItem.getDeprecatedVersion());
		} else {
			return null;
		}
	}

	@ExternalizedProperty
	public boolean isQualifiable() {
		return codeListItem.isQualifiable();
	}

	@ExternalizedProperty
	public String getCode() {
		return codeListItem.getCode();
	}

	@ExternalizedProperty
	public List<LanguageSpecificTextProxy> getLabels() {
		return LanguageSpecificTextProxy.fromList(codeListItem.getLabels());
	}

	@ExternalizedProperty
	public List<LanguageSpecificTextProxy> getDescriptions() {
		return LanguageSpecificTextProxy.fromList(codeListItem.getDescriptions());
	}

	@ExternalizedProperty
	public List<CodeListItemProxy> getChildItems() {
		return CodeListItemProxy.fromList(codeListItem.getChildItems());
	}

	
}
