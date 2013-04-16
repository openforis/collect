/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.RecordUpdateResponseSet;
import org.openforis.collect.spring.MessageContextHolder;

/**
 * @author S. Ricci
 *
 */
public class RecordUpdateResponseSetProxy implements Proxy {

	private transient MessageContextHolder messageContextHolder;
	private transient RecordUpdateResponseSet responseSet;
	
	public RecordUpdateResponseSetProxy(
			MessageContextHolder messageContextHolder,
			RecordUpdateResponseSet responseSet) {
		super();
		this.messageContextHolder = messageContextHolder;
		this.responseSet = responseSet;
	}

	@ExternalizedProperty
	public List<NodeUpdateResponseProxy<?>> getResponses() {
		return NodeUpdateResponseProxy.fromList(messageContextHolder, responseSet.getResponses());
	}

	@ExternalizedProperty
	public Integer getErrors() {
		return responseSet.getErrors();
	}

	@ExternalizedProperty
	public Integer getSkipped() {
		return responseSet.getSkipped();
	}

	@ExternalizedProperty
	public Integer getMissing() {
		return responseSet.getMissing();
	}

	@ExternalizedProperty
	public Integer getWarnings() {
		return responseSet.getWarnings();
	}

	@ExternalizedProperty
	public Integer getMissingErrors() {
		return responseSet.getMissingErrors();
	}

	@ExternalizedProperty
	public Integer getMissingWarnings() {
		return responseSet.getMissingWarnings();
	}
	
}
