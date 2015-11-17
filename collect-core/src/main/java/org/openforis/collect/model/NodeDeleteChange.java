package org.openforis.collect.model;

import java.util.List;

import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.idm.model.Node;

/**
 * Change related to the delete of a Node in a Record.
 * 
 * @author S. Ricci
 *
 */
public class NodeDeleteChange extends NodeChange<Node<?>> {
	
	private Step step;

	public NodeDeleteChange(Integer recordId, Step step, List<Integer> ancestoIds, Node<?> node) {
		super(recordId, ancestoIds, node);
		this.step = step;
	}
	
	@Override
	public Step getRecordStep() {
		return step;
	}
	
}