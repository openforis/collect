package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.RecordUpdateResponse.AddEntityResponse;
import org.openforis.collect.spring.MessageContextHolder;

/**
 * 
 * @author S. Ricci
 *
 */
public class AddEntityResponseProxy extends EntityUpdateResponseProxy implements AddNodeResponseProxy {

	public AddEntityResponseProxy(
			MessageContextHolder messageContextHolder,
			AddEntityResponse response) {
		super(messageContextHolder, response);
	}

	@Override
	@ExternalizedProperty
	public NodeProxy getCreatedNode() {
		return NodeProxy.fromNode(messageContextHolder, response.getNode());
	}
	
}