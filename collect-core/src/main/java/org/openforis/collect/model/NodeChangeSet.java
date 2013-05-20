/**
 * 
 */
package org.openforis.collect.model;

import java.util.List;

import org.openforis.commons.collection.CollectionUtils;

/**
 * Collection of NodeChange objects.
 * 
 * @author S. Ricci
 *
 */
public class NodeChangeSet {

	private List<NodeChange<?>> changes;
	
	public NodeChangeSet(List<NodeChange<?>> changes) {
		this.changes = changes;
	}
	
	public List<NodeChange<?>> getChanges() {
		return CollectionUtils.unmodifiableList(changes);
	}

	public boolean isEmpty() {
		return changes.isEmpty();
	}
	
	public int size() {
		return changes.size();
	}
	
}
