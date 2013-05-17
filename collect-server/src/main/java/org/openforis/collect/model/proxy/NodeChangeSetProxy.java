/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.collect.spring.MessageContextHolder;

/**
 * @author S. Ricci
 *
 */
public class NodeChangeSetProxy implements Proxy {

	private transient MessageContextHolder messageContextHolder;
	private transient NodeChangeSet changeSet;
	
	public NodeChangeSetProxy(
			MessageContextHolder messageContextHolder,
			NodeChangeSet changeSet) {
		super();
		this.messageContextHolder = messageContextHolder;
		this.changeSet = changeSet;
	}

	@ExternalizedProperty
	public List<NodeChangeProxy<?>> getChanges() {
		return NodeChangeProxy.fromList(messageContextHolder, changeSet.getChanges());
	}

	@ExternalizedProperty
	public Integer getErrors() {
		return changeSet.getErrors();
	}

	@ExternalizedProperty
	public Integer getSkipped() {
		return changeSet.getSkipped();
	}

	@ExternalizedProperty
	public Integer getMissing() {
		return changeSet.getMissing();
	}

	@ExternalizedProperty
	public Integer getWarnings() {
		return changeSet.getWarnings();
	}

	@ExternalizedProperty
	public Integer getMissingErrors() {
		return changeSet.getMissingErrors();
	}

	@ExternalizedProperty
	public Integer getMissingWarnings() {
		return changeSet.getMissingWarnings();
	}
	
	@ExternalizedProperty
	public boolean isRecordSaved() {
		return changeSet.isRecordSaved();
	}
}
