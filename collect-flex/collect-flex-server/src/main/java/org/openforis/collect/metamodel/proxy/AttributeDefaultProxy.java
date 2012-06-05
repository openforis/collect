/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.metamodel.AttributeDefault;

/**
 * @author M. Togna
 * 
 */
public class AttributeDefaultProxy implements Proxy {

	private transient AttributeDefault attributeDefault;

	public AttributeDefaultProxy(AttributeDefault attributeDefault) {
		super();
		this.attributeDefault = attributeDefault;
	}

	static List<AttributeDefaultProxy> fromList(List<AttributeDefault> list) {
		List<AttributeDefaultProxy> proxies = new ArrayList<AttributeDefaultProxy>();
		if (list != null) {
			for (AttributeDefault d : list) {
				proxies.add(new AttributeDefaultProxy(d));
			}
		}
		return proxies;
	}

	@ExternalizedProperty
	public String getValue() {
		return attributeDefault.getValue();
	}

	@ExternalizedProperty
	public String getExpression() {
		return attributeDefault.getExpression();
	}

	@ExternalizedProperty
	public String getCondition() {
		return attributeDefault.getCondition();
	}

}
