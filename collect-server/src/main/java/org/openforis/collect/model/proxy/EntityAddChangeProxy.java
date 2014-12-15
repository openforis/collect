package org.openforis.collect.model.proxy;

import java.util.Locale;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.EntityChange;

/**
 * 
 * @author S. Ricci
 *
 */
public class EntityAddChangeProxy extends EntityChangeProxy implements NodeAddChangeProxy {

	public EntityAddChangeProxy(EntityChange change, Locale locale) {
		super(change, locale);
	}

	@Override
	@ExternalizedProperty
	public NodeProxy getCreatedNode() {
		return NodeProxy.fromNode(change.getNode(), getLocale());
	}
	
}