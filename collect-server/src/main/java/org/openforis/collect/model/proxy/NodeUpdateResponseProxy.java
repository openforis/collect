package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.NodeUpdateResponse;
import org.openforis.collect.model.NodeUpdateResponse.AttributeAddResponse;
import org.openforis.collect.model.NodeUpdateResponse.EntityAddResponse;
import org.openforis.collect.model.NodeUpdateResponse.AttributeUpdateResponse;
import org.openforis.collect.model.NodeUpdateResponse.NodeDeleteResponse;
import org.openforis.collect.model.NodeUpdateResponse.EntityUpdateResponse;
import org.openforis.collect.spring.MessageContextHolder;

/**
 * 
 * @author S. Ricci
 *
 * @param <T> NodeUpdateResponse
 */
public class NodeUpdateResponseProxy<T extends NodeUpdateResponse<?>> implements Proxy {

	protected transient MessageContextHolder messageContextHolder;
	protected transient T response;

	public NodeUpdateResponseProxy(MessageContextHolder messageContextHolder, T response) {
		this.response = response;
		this.messageContextHolder = messageContextHolder;
	}

	public static List<NodeUpdateResponseProxy<?>> fromList(MessageContextHolder messageContextHolder, Collection<NodeUpdateResponse<?>> items) {
		List<NodeUpdateResponseProxy<?>> result = new ArrayList<NodeUpdateResponseProxy<?>>();
		if ( items != null ) {
			for (NodeUpdateResponse<?> item : items) {
				NodeUpdateResponseProxy<?> proxy;
				if ( item instanceof AttributeAddResponse ) {
					proxy = new AttributeAddResponseProxy(messageContextHolder, (AttributeAddResponse) item);
				} else if ( item instanceof EntityAddResponse ) {
					proxy = new EntityAddResponseProxy(messageContextHolder, (EntityAddResponse) item);
				} else if ( item instanceof AttributeUpdateResponse ) {
					proxy = new AttributeUpdateResponseProxy(messageContextHolder, (AttributeUpdateResponse) item);
				} else if ( item instanceof EntityUpdateResponse ) {
					proxy = new EntityUpdateResponseProxy(messageContextHolder, (EntityUpdateResponse) item);
				} else if ( item instanceof NodeDeleteResponse ) {
					proxy = new NodeDeleteResponseProxy(messageContextHolder, (NodeDeleteResponse) item);
				} else {
					throw new IllegalArgumentException("RecordUpdateResponse not supported: " + item.getClass().getSimpleName());
				}
				result.add(proxy);
			}
		}
		return result;
	}
	
	@ExternalizedProperty
	public int getNodeId() {
		return response.getNode().getInternalId();
	}

}