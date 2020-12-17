/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.openforis.collect.Proxy;
import org.openforis.collect.ProxyContext;
import org.openforis.collect.manager.MessageSource;
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

	private transient Node<?> node;
	protected transient ProxyContext context;

	public static NodeProxy fromNode(Node<?> node, ProxyContext context) {
		return fromNode(null, node, context);
	}

	public static NodeProxy fromNode(EntityProxy parent, Node<?> node, ProxyContext context) {
		if (node instanceof Attribute<?, ?>) {
			if (node instanceof CodeAttribute) {
				return new CodeAttributeProxy(parent, (CodeAttribute) node, context);
			} else {
				return new AttributeProxy(parent, (Attribute<?, ?>) node, context);
			}
		} else if (node instanceof Entity) {
			return new EntityProxy(parent, (Entity) node, context);
		} else {
			throw new IllegalArgumentException("Unsupported node type: " + node.getClass().getName());
		}
	}

	public static List<NodeProxy> fromList(EntityProxy parent, List<Node<?>> list, ProxyContext context) {
		List<NodeProxy> result = new ArrayList<NodeProxy>();
		if (list != null) {
			for (Node<?> node : list) {
				NodeProxy proxy = fromNode(node, context);
				if (node instanceof Attribute<?, ?>) {
					if (node instanceof CodeAttribute) {
						proxy = new CodeAttributeProxy(parent, (CodeAttribute) node, context);
					} else {
						proxy = new AttributeProxy(parent, (Attribute<?, ?>) node, context);
					}
				} else {
					proxy = new EntityProxy(parent, (Entity) node, context);
				}
				result.add(proxy);
			}
		}
		return result;
	}

	public NodeProxy(EntityProxy parent, Node<?> node, ProxyContext context) {
		super();
		this.node = node;
		this.context = context;
	}

	public Integer getId() {
		return node.getInternalId();
	}

	public Integer getDefinitionId() {
		return node.getDefinition() == null ? null : node.getDefinition().getId();
	}

	public String getPath() {
		return node.getPath();
	}

	public Integer getParentId() {
		return node.getParent() == null ? null : node.getParent().getInternalId();
	}

	public boolean isUserSpecified() {
		return node.isUserSpecified();
	}

	protected MessageSource getMessageSource() {
		return context.getMessageSource();
	}

	protected Locale getLocale() {
		return context.getLocale();
	}
}
