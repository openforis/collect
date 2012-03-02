package org.openforis.collect.remoting.service;

import java.util.List;

import org.openforis.collect.model.proxy.NodeProxy;

/**
 * 
 * @author S. Ricci
 *
 */
public class UpdateResponse {

	private List<NodeProxy> addedNodes;
	private List<NodeProxy> updatedNodes;
	private Integer[] deletedNodeIds;
	
	public UpdateResponse() {
		super();
	}

	public List<NodeProxy> getUpdatedNodes() {
		return updatedNodes;
	}

	public void setUpdatedNodes(List<NodeProxy> addedNodes) {
		this.updatedNodes = addedNodes;
	}

	public Integer[] getDeletedNodeIds() {
		return deletedNodeIds;
	}

	public void setDeletedNodeIds(Integer[] deletedNodeIds) {
		this.deletedNodeIds = deletedNodeIds;
	}

	public List<NodeProxy> getAddedNodes() {
		return addedNodes;
	}

	public void setAddedNodes(List<NodeProxy> addedNodes) {
		this.addedNodes = addedNodes;
	}

	
}
