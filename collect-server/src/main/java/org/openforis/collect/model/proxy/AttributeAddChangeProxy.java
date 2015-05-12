package org.openforis.collect.model.proxy;

import java.util.Locale;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.AttributeChange;

/**
 * 
 * @author S. Ricci
 *
 */
public class AttributeAddChangeProxy extends AttributeChangeProxy implements NodeAddChangeProxy {

	public AttributeAddChangeProxy(AttributeChange change, Locale locale) {
		super(change, locale);
	}

	@Override
	@ExternalizedProperty
	public NodeProxy getCreatedNode() {
		return NodeProxy.fromNode(change.getNode(), getLocale());
	}

}