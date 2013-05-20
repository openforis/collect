/**
 * 
 */
package org.openforis.collect.model;

import java.util.List;

import org.openforis.collect.model.NodeChange.AttributeAddChange;
import org.openforis.collect.model.NodeChange.AttributeChange;
import org.openforis.collect.model.NodeChange.EntityAddChange;
import org.openforis.collect.model.NodeChange.EntityChange;
import org.openforis.collect.model.NodeChange.NodeDeleteChange;
import org.openforis.idm.model.Node;

/**
 * @author S. Ricci
 *
 */
public class NodeChangeSet {

	private NodeChangeMap changeMap;
	
	public NodeChangeSet() {
		this(new NodeChangeMap());
	}
	
	public NodeChangeSet(NodeChangeMap changeMap) {
		this.changeMap = changeMap;
	}
	
	public List<NodeChange<?>> getValues() {
		return changeMap.values();
	}

	public void addChange(NodeChange<?> c) {
		NodeChange<?> oldItem = changeMap.getChange(c.getNode());
		if ( oldItem == null || ! (oldItem instanceof NodeDeleteChange) ) {
			if ( oldItem instanceof AttributeAddChange && c instanceof AttributeChange ) {
				((AttributeAddChange) oldItem).merge((AttributeChange) c);
			} else if ( oldItem instanceof EntityAddChange && c instanceof EntityChange ) {
				((EntityAddChange) oldItem).merge((EntityChange) c);
			} else {
				changeMap.putChange(c);
			}
		}
	}
	
	public NodeChange<?> getChange(Node<?> node) {
		return changeMap.getChange(node);
	}
	
}
