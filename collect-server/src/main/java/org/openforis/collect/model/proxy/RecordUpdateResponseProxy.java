/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.RecordUpdateResponse;
import org.openforis.collect.spring.MessageContextHolder;
import org.openforis.idm.model.Attribute;

/**
 * @author S. Ricci
 *
 */
public class RecordUpdateResponseProxy implements Proxy {

	private transient MessageContextHolder messageContextHolder;
	private transient RecordUpdateResponse response;

	public RecordUpdateResponseProxy(MessageContextHolder messageContextHolder, RecordUpdateResponse response) {
		super();
		this.response = response;
		this.messageContextHolder = messageContextHolder;
	}

	public static List<RecordUpdateResponseProxy> fromList(MessageContextHolder messageContextHolder, List<RecordUpdateResponse> items) {
		List<RecordUpdateResponseProxy> result = new ArrayList<RecordUpdateResponseProxy>();
		if ( items != null ) {
			for (RecordUpdateResponse item : items) {
				RecordUpdateResponseProxy proxy = new RecordUpdateResponseProxy(messageContextHolder, item);
				result.add(proxy);
			}
		}
		return result;
	}
	
	@ExternalizedProperty
	public int getNodeId() {
		return response.getNode().getInternalId();
	}

	@ExternalizedProperty
	public Map<String, Object> getRelevant() {
		return response.getRelevant();
	}

	@ExternalizedProperty
	public Map<String, Object> getRequired() {
		return response.getRequired();
	}

	@ExternalizedProperty
	public ValidationResultsProxy getValidationResults() {
		if ( response.getValidationResults() == null ) {
			return null;
		} else {
			return new ValidationResultsProxy(messageContextHolder, (Attribute<?, ?>) response.getNode(), response.getValidationResults());
		}
	}

	@ExternalizedProperty
	public Map<String, Object> getMinCountValidation() {
		return response.getMinCountValidation();
	}

	@ExternalizedProperty
	public Map<String, Object> getMaxCountValidation() {
		return response.getMaxCountValidation();
	}

	@ExternalizedProperty
	public NodeProxy getCreatedNode() {
		return NodeProxy.fromNode(messageContextHolder, response.getNode());
	}

	@ExternalizedProperty
	public Integer getDeletedNodeId() {
		return response.getDeletedNodeId();
	}

	@ExternalizedProperty
	public Map<Integer, Object> getUpdatedFieldValues() {
		return response.getUpdatedFieldValues();
	}
	
	
	
}
