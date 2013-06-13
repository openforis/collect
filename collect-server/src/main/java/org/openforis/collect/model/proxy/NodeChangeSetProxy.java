/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.NodeChangeSet;

/**
 * @author S. Ricci
 *
 */
public class NodeChangeSetProxy implements Proxy {

	private transient NodeChangeSet changeSet;
	private transient CollectRecord record;
	private boolean recordSaved;
	
	public NodeChangeSetProxy(
			CollectRecord record,
			NodeChangeSet changeSet) {
		super();
		this.record = record;
		this.changeSet = changeSet;
	}

	@ExternalizedProperty
	public List<NodeChangeProxy<?>> getChanges() {
		return NodeChangeProxy.fromList(changeSet.getChanges());
	}

	public boolean isRecordSaved() {
		return recordSaved;
	}
	
	public void setRecordSaved(boolean recordSaved) {
		this.recordSaved = recordSaved;
	}

	@ExternalizedProperty
	public Integer getErrors() {
		return record.getErrors();
	}

	@ExternalizedProperty
	public Integer getSkipped() {
		return record.getSkipped();
	}

	@ExternalizedProperty
	public Integer getMissing() {
		return record.getMissing();
	}

	@ExternalizedProperty
	public Integer getWarnings() {
		return record.getWarnings();
	}

	@ExternalizedProperty
	public Integer getMissingErrors() {
		return record.getMissingErrors();
	}

	@ExternalizedProperty
	public Integer getMissingWarnings() {
		return record.getMissingWarnings();
	}
	
}
