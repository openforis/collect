package org.openforis.collect.remoting.service;

import java.util.List;

import org.openforis.collect.model.proxy.NodeProxy;
import org.openforis.collect.model.proxy.NodeStateProxy;

/**
 * 
 * @author S. Ricci
 *
 */
public class UpdateResponse {

	private List<NodeStateProxy> states;
	private List<NodeProxy> addedNodes;
	private List<NodeProxy> deletedNodes;
	
	public UpdateResponse(List<NodeStateProxy> states) {
		super();
		this.states = states;
	}

	public List<NodeStateProxy> getStates() {
		return states;
	}

	public void setStates(List<NodeStateProxy> states) {
		this.states = states;
	}

	public List<NodeProxy> getAddedNodes() {
		return addedNodes;
	}

	public void setAddedNodes(List<NodeProxy> addedNodes) {
		this.addedNodes = addedNodes;
	}

	public List<NodeProxy> getDeletedNodes() {
		return deletedNodes;
	}

	public void setDeletedNodes(List<NodeProxy> deletedNodes) {
		this.deletedNodes = deletedNodes;
	}

	
}
