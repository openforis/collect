/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
import org.openforis.collect.spring.SpringMessageSource;
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
	
	public CodeAttributeProxy(SpringMessageSource messageContextHolder, EntityProxy parent, CodeAttribute attribute) {
		super(messageContextHolder, parent, attribute);
		this.codeAttribute = attribute;
	}

	@ExternalizedProperty
	public CodeListItemProxy getCodeListItem() {
		if (! isExternalCodeList() ) {
			CodeListItem codeListItem = codeAttribute.getCodeListItem();
			return codeListItem == null ? null: new CodeListItemProxy(codeListItem);
		} else {
			return null;
		}
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
