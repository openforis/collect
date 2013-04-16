package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.NodeUpdateResponse;
import org.openforis.collect.model.NodeUpdateResponse.AddAttributeResponse;
import org.openforis.collect.model.NodeUpdateResponse.AddEntityResponse;
import org.openforis.collect.model.NodeUpdateResponse.AttributeUpdateResponse;
import org.openforis.collect.model.NodeUpdateResponse.DeleteNodeResponse;
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
				if ( item instanceof AddAttributeResponse ) {
					proxy = new AddAttributeResponseProxy(messageContextHolder, (AddAttributeResponse) item);
				} else if ( item instanceof AddEntityResponse ) {
					proxy = new AddEntityResponseProxy(messageContextHolder, (AddEntityResponse) item);
				} else if ( item instanceof AttributeUpdateResponse ) {
					proxy = new AttributeUpdateResponseProxy(messageContextHolder, (AttributeUpdateResponse) item);
				} else if ( item instanceof EntityUpdateResponse ) {
					proxy = new EntityUpdateResponseProxy(messageContextHolder, (EntityUpdateResponse) item);
				} else if ( item instanceof DeleteNodeResponse ) {
					proxy = new DeleteNodeResponseProxy(messageContextHolder, (DeleteNodeResponse) item);
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