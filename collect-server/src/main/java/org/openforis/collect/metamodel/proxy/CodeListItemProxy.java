/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Node;

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

	public static List<CodeListItemProxy> fromList(List<CodeListItem> list) {
		List<CodeListItemProxy> proxies = new ArrayList<CodeListItemProxy>();
		if (list != null) {
			for (CodeListItem item : list) {
				CodeListItemProxy proxy = new CodeListItemProxy(item);
				proxies.add(proxy);
			}
		}
		return proxies;
	}
	
	public static void setSelectedItems(List<CodeListItemProxy> proxies, List<Node<? extends NodeDefinition>> codes) {
		for (CodeListItemProxy proxy : proxies) {
			//if code in attributes, set selected and qualifier in proxy
			for (Node<? extends NodeDefinition> node : codes) {
				CodeAttribute code = (CodeAttribute) node;
				Code codeVal = code.getValue();
				if(codeVal != null && codeVal.getCode() != null && codeVal.getCode().equals(proxy.getCode())) {
					proxy.setSelected(Boolean.TRUE);
					proxy.setQualifier(codeVal.getQualifier());
				}
			}
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
