/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.ProxyContext;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
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

	public boolean isEnumerator() {
		return codeAttribute.isEnumerator();
	}

	protected boolean isExternalCodeList() {
		CodeAttributeDefinition defn = codeAttribute.getDefinition();
		CodeList list = defn.getList();
		return list.isExternal();
	}
	
}
