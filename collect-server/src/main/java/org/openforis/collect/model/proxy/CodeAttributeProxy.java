/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.ProxyContext;
import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.model.CodeAttribute;

/**
 * @author S. Ricci
 *
 */
public class CodeAttributeProxy extends AttributeProxy {

	private transient CodeAttribute codeAttribute;
	
	public CodeAttributeProxy(EntityProxy parent,
			CodeAttribute attribute, ProxyContext context) {
		super(parent, attribute, context);
		this.codeAttribute = attribute;
	}

	@ExternalizedProperty
	public CodeListItemProxy getCodeListItem() {
		if ( isEnumerator() ) {
			CodeListService codeListManager = getCodeListService();
			CodeListItem codeListItem = codeListManager.loadItem(codeAttribute);
			return codeListItem == null ? null: new CodeListItemProxy(codeListItem);
		} else {
			return null;
		}
	}

	private CodeListService getCodeListService() {
		return context.getSurveyContext().getCodeListService();
	}

	@ExternalizedProperty
	public boolean isEnumerator() {
		return codeAttribute.isEnumerator();
	}

	protected boolean isExternalCodeList() {
		CodeAttributeDefinition defn = codeAttribute.getDefinition();
		CodeList list = defn.getList();
		return list.isExternal();
	}
	
}
