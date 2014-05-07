package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.AttributeAddChange;
import org.openforis.collect.model.AttributeChange;
import org.openforis.collect.model.EntityAddChange;
import org.openforis.collect.model.EntityChange;
import org.openforis.collect.model.NodeChange;
import org.openforis.collect.model.NodeDeleteChange;

/**
 * 
 * @author S. Ricci
 *
 * @param <T> NodeChange
 */
public class NodeChangeProxy<C extends NodeChange<?>> implements Proxy {

	protected transient C change;
	private Locale locale;

	public NodeChangeProxy(C change, Locale locale) {
		this.change = change;
		this.locale = locale;
	}

	public static List<NodeChangeProxy<?>> fromList(Collection<NodeChange<?>> items, Locale locale) {
		List<NodeChangeProxy<?>> result = new ArrayList<NodeChangeProxy<?>>();
		if ( items != null ) {
			for (NodeChange<?> item : items) {
				NodeChangeProxy<?> proxy;
				if ( item instanceof AttributeAddChange ) {
					proxy = new AttributeAddChangeProxy((AttributeAddChange) item, locale);
				} else if ( item instanceof EntityAddChange ) {
					proxy = new EntityAddChangeProxy((EntityAddChange) item, locale);
				} else if ( item instanceof AttributeChange ) {
					proxy = new AttributeChangeProxy((AttributeChange) item, locale);
				} else if ( item instanceof EntityChange) {
					proxy = new EntityChangeProxy((EntityChange) item, locale);
				} else if ( item instanceof NodeDeleteChange ) {
					proxy = new NodeDeleteChangeProxy((NodeDeleteChange) item, locale);
				} else {
					throw new IllegalArgumentException("NodeChange type not supported: " + item.getClass().getSimpleName());
				}
				result.add(proxy);
			}
		}
		return result;
	}
	
	@ExternalizedProperty
	public int getNodeId() {
		return change.getNode().getInternalId();
	}
	
	public Locale getLocale() {
		return locale;
	}

}