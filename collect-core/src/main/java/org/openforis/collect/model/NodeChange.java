package org.openforis.collect.model;


import java.util.List;

import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.idm.model.Node;

/**
 * Change related to a Node in a Record.
 * 
 * 
 * @author S. Ricci
 */
public abstract class NodeChange<T extends Node<?>> {

	protected final Integer recordId;
	protected final List<Integer> ancestorIds;
	protected final T node;
	
	public NodeChange(Integer recordId, List<Integer> ancestorIds, T node) {
		this.recordId = recordId;
		this.ancestorIds = ancestorIds;
		this.node = node;
	}

	public Integer getRecordId() {
		return recordId;
	}
	
	public List<Integer> getAncestorIds() {
		return ancestorIds;
	}
	
	public Integer getParentId() {
		return ancestorIds.isEmpty() ? null : ancestorIds.get(0);
	}

	public Step getRecordStep() {
		CollectRecord record = (CollectRecord) node.getRecord();
		return record.getStep();
	}
	
	public T getNode() {
		return node;
	}
	
	@Override
	public String toString() {
		String prettyFormatPath = node.isDetached() ? "[detached]/" + node.getName() : node.getPath();
		return String.format("%s for node %s", 
				this.getClass().getSimpleName(), 
				prettyFormatPath);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeChange<?> other = (NodeChange<?>) obj;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
	}
	
}