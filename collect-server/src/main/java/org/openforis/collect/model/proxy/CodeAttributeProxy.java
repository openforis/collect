/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.model.CodeAttribute;

/**
 * @author S. Ricci
 *
 */
public class CodeAttributeProxy extends AttributeProxy {

	private transient CodeAttribute codeAttribute;
	
	public CodeAttributeProxy(EntityProxy parent,
			CodeAttribute attribute) {
		super(parent, attribute);
		this.codeAttribute = attribute;
	}

	@ExternalizedProperty
	public CodeListItemProxy getCodeListItem() {
		if (! isExternalCodeList() ) {
			CodeListManager codeListManager = getCodeListManager();
			CodeListItem codeListItem = codeListManager.loadItemByAttribute(codeAttribute);
			return codeListItem == null ? null: new CodeListItemProxy(codeListItem);
		} else {
			return null;
		}
	}

	private CodeListManager getCodeListManager() {
		return getContextBean(CodeListManager.class);
	}

	@ExternalizedProperty
	public boolean isEnumerator() {
		CodeAttributeDefinition definition = codeAttribute.getDefinition();
		EntityDefinition parentDefinition = (EntityDefinition) definition.getParentDefinition();
		if(parentDefinition.isEnumerable() && definition.isKey() && 
				definition.getList() != null && ! definition.getList().isExternal()) {
			return true;
		} else {
			return false;
		}
	}

	protected boolean isExternalCodeList() {
		CodeAttributeDefinition defn = codeAttribute.getDefinition();
		CodeList list = defn.getList();
		return list.isExternal();
	}
	
}
