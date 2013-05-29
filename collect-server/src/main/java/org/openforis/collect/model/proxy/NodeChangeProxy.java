package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.AttributeAddChange;
import org.openforis.collect.model.AttributeChange;
import org.openforis.collect.model.EntityAddChange;
import org.openforis.collect.model.EntityChange;
import org.openforis.collect.model.NodeChange;
import org.openforis.collect.model.NodeDeleteChange;
import org.openforis.collect.spring.SpringMessageSource;

/**
 * 
 * @author S. Ricci
 *
 * @param <T> NodeChange
 */
public class NodeChangeProxy<C extends NodeChange<?>> implements Proxy {

	protected transient SpringMessageSource messageContextHolder;
	protected transient C change;

	public NodeChangeProxy(SpringMessageSource messageContextHolder, C change) {
		this.change = change;
		this.messageContextHolder = messageContextHolder;
	}

	public static List<NodeChangeProxy<?>> fromList(SpringMessageSource messageContextHolder, Collection<NodeChange<?>> items) {
		List<NodeChangeProxy<?>> result = new ArrayList<NodeChangeProxy<?>>();
		if ( items != null ) {
			for (NodeChange<?> item : items) {
				NodeChangeProxy<?> proxy;
				if ( item instanceof AttributeAddChange ) {
					proxy = new AttributeAddChangeProxy(messageContextHolder, (AttributeAddChange) item);
				} else if ( item instanceof EntityAddChange ) {
					proxy = new EntityAddChangeProxy(messageContextHolder, (EntityAddChange) item);
				} else if ( item instanceof AttributeChange ) {
					proxy = new AttributeChangeProxy(messageContextHolder, (AttributeChange) item);
				} else if ( item instanceof EntityChange) {
					proxy = new EntityChangeProxy(messageContextHolder, (EntityChange) item);
				} else if ( item instanceof NodeDeleteChange ) {
					proxy = new NodeDeleteChangeProxy(messageContextHolder, (NodeDeleteChange) item);
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

}