/**
 * 
 */
package org.openforis.collect.model;

import java.util.Set;

import org.openforis.idm.model.Node;

/**
 * Collection of NodeChange objects.
 * 
 * @author S. Ricci
 *
 */
public interface NodeChangeSet {

	public Set<NodeChange<?>> getChanges();

	public NodeChange<?> getChange(Node<?> node);

	public NodeChange<?> getChange(int nodeId);
	
	public boolean isEmpty();
	
	public int size();

}
