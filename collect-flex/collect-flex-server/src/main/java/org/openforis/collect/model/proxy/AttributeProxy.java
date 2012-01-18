/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.Proxy;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.model.Attribute;

/**
 * @author M. Togna
 * 
 */
public class AttributeProxy implements Proxy {

	private transient Attribute<? extends AttributeDefinition, ?> attribute;

	public AttributeProxy(Attribute<? extends AttributeDefinition, ?> attribute) {
		this.attribute = attribute;
	}

}
