/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.spring.MessageContextHolder;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * @author S. Ricci
 * @author M. Togna
 *
 */
public class NodeProxy implements Proxy {

	protected transient MessageContextHolder messageContextHolder;
	
	private EntityProxy parent;
	private transient Node<?> node;
	
	public static NodeProxy fromNode(MessageContextHolder messageContextHolder, Node<?> node) {
		if (node instanceof Attribute<?, ?>) {
			return new AttributeProxy(messageContextHolder, null, (Attribute<?, ?>) node);
		} else if (node instanceof Entity) {
			return new EntityProxy(messageContextHolder, null, (Entity) node);
		}
		return null;
	}
	
	public NodeProxy(MessageContextHolder messageContextHolder, EntityProxy parent, Node<?> node) {
		super();
		this.messageContextHolder = messageContextHolder;
		this.parent = parent;
		this.node = node;
	}

	public static List<NodeProxy> fromList(MessageContextHolder messageContextHolder, EntityProxy parent, List<Node<?>> list) {
		List<NodeProxy> result = new ArrayList<NodeProxy>();
		if(list != null) {
			for (Node<?> node : list) {
				NodeProxy proxy;
				if(node instanceof Attribute<?, ?>) {
					if(node instanceof CodeAttribute) {
						proxy = new CodeAttributeProxy(messageContextHolder, parent, (CodeAttribute) node);
					} else {
						proxy = new AttributeProxy(messageContextHolder, parent, (Attribute<?, ?>) node);
					}
				} else {
					proxy = new EntityProxy(messageContextHolder, parent, (Entity) node);
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

	public EntityProxy getParent() {
		return parent;
	}
	
	public void setParent(EntityProxy parent) {
		this.parent = parent;
	}
}
