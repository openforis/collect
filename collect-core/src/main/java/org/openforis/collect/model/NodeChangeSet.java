/**
 * 
 */
package org.openforis.collect.model;

import java.util.List;

import org.openforis.idm.model.Node;

/**
 * Collection of NodeChange objects.
 * 
 * @author S. Ricci
 *
 */
public interface NodeChangeSet {

	/**
	 * List of changes. Then have to be in order (for example after a node insert there can be nested node changes).
	 * @return
	 */
	public List<NodeChange<?>> getChanges();

	public NodeChange<?> getChange(Node<?> node);

	public NodeChange<?> getChange(int nodeId);
	
	public boolean isEmpty();
	
	public int size();

}
