/**
 * 
 */
package org.openforis.collect.model;

import java.util.List;
import java.util.Set;

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
	List<NodeChange<?>> getChanges();

	Set<Node<?>> getChangedNodes();

	NodeChange<?> getChange(Node<?> node);

	NodeChange<?> getChange(int nodeId);
	
	boolean isEmpty();
	
	int size();

}
