/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.granite.context.GraniteContext;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.openforis.collect.Proxy;
import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.spring.SpringMessageSource;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author S. Ricci
 * @author M. Togna
 *
 */
public class NodeProxy implements Proxy {

	private EntityProxy parent;
	private transient Node<?> node;
	
	public static NodeProxy fromNode(Node<?> node) {
		if (node instanceof Attribute<?, ?>) {
			return new AttributeProxy(null, (Attribute<?, ?>) node);
		} else if (node instanceof Entity) {
			return new EntityProxy(null, (Entity) node);
		}
		return null;
	}
	
	public NodeProxy(EntityProxy parent, Node<?> node) {
		super();
		this.parent = parent;
		this.node = node;
	}

	public static List<NodeProxy> fromList(
			EntityProxy parent,
			List<Node<?>> list) {
		List<NodeProxy> result = new ArrayList<NodeProxy>();
		if(list != null) {
			for (Node<?> node : list) {
				NodeProxy proxy;
				if(node instanceof Attribute<?, ?>) {
					if(node instanceof CodeAttribute) {
						proxy = new CodeAttributeProxy(parent, (CodeAttribute) node);
					} else {
						proxy = new AttributeProxy(parent, (Attribute<?, ?>) node);
					}
				} else {
					proxy = new EntityProxy(parent, (Entity) node);
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
	
	protected MessageSource getMessageSource() {
		Class<SpringMessageSource> type = SpringMessageSource.class;
		return getContextBean(type);
	}

	protected <T extends Object> T getContextBean(Class<T> type) {
		HttpGraniteContext graniteContext = (HttpGraniteContext) GraniteContext.getCurrentInstance();
		ServletContext servletContext = graniteContext.getServletContext();
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		T bean = applicationContext.getBean(type);
		return bean;
	}

	public EntityProxy getParent() {
		return parent;
	}
	
	public void setParent(EntityProxy parent) {
		this.parent = parent;
	}
}
