/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.state.NodeState;

/**
 * @author S. Ricci
 * @author M. Togna
 *
 */
public class NodeProxy implements Proxy {

	private transient Node<?> node;
	
	private NodeStateProxy state;
	
	public NodeProxy(Node<?> node) {
		super();
		this.node = node;
		
		/*
		CollectRecord record = (CollectRecord) this.node.getRecord();
		NodeState nodeState = record.getNodeState(node);
		if (nodeState != null) {
			state = new NodeStateProxy(nodeState);
		}
		*/
	}

	public static List<NodeProxy> fromList(List<Node<?>> list) {
		List<NodeProxy> result = new ArrayList<NodeProxy>();
		if(list != null) {
			for (Node<?> node : list) {
				NodeProxy proxy;
				if(node instanceof Attribute<?, ?>) {
					proxy = new AttributeProxy((Attribute<?, ?>) node);
				} else {
					proxy = new EntityProxy((Entity) node);
				}
				result.add(proxy);
			}
		}
		return result;
	}
	
	@ExternalizedProperty
	public Integer getId() {
		return node.getInternalId();
	}

	@ExternalizedProperty
	public String getName() {
		return node.getName();
	}
	
	@ExternalizedProperty
	public Integer getDefinitionId() {
		if(node.getDefinition() != null) {
			return node.getDefinition().getId();
		} else {
			return null;
		}
	}
	
	@ExternalizedProperty
	public Integer getParentId() {
		if(node.getParent() != null) {
			return node.getParent().getInternalId();
		} else {
			return null;
		}
	}
	
	public NodeStateProxy getState() {
		return state;
	}
	
	public void setState(NodeStateProxy state) {
		this.state = state;
	}
	
}
