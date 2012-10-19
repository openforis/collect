/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.apache.commons.lang3.StringUtils;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.model.CodeAttribute;

/**
 * @author S. Ricci
 *
 */
public class CodeAttributeProxy extends AttributeProxy {

	private transient CodeAttribute codeAttribute;
	
	public CodeAttributeProxy(EntityProxy parent, CodeAttribute attribute) {
		super(parent, attribute);
		this.codeAttribute = attribute;
	}

	@ExternalizedProperty
	public CodeListItemProxy getCodeListItem() {
		CodeListItem codeListItem = codeAttribute.getCodeListItem();
		if(codeListItem != null) {
			return new CodeListItemProxy(codeListItem);
		} else {
			return null;
		}
	}
	
	@ExternalizedProperty
	public boolean isEnumerator() {
		CodeAttributeDefinition definition = codeAttribute.getDefinition();
		EntityDefinition parentDefinition = (EntityDefinition) definition.getParentDefinition();
		if(parentDefinition.isEnumerable() && definition.isKey() && 
				definition.getList() != null && StringUtils.isBlank(definition.getList().getLookupTable())) {
			return true;
		} else {
			return false;
		}
	}
	
}
