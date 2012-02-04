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

	private Boolean selected;
	private String qualifier;
	
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

	public Boolean getSelected() {
		return selected;
	}
	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

	public String getQualifier() {
		return qualifier;
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}
	
}
