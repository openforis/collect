/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.model.Node;

/**
 * @author S. Ricci
 *
 */
public class NodeProxy implements Proxy {

	private transient Node<?> node;

	public NodeProxy(Node<?> node) {
		super();
		this.node = node;
	}

	@ExternalizedProperty
	public Integer getId() {
		return node.getId();
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
			return node.getParent().getId();
		} else {
			return null;
		}
	}
	
}
