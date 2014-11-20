/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

	private transient Node<?> node;
	private transient Locale locale;
	
	public static NodeProxy fromNode(Node<?> node, Locale locale) {
		if (node instanceof Attribute<?, ?>) {
			return new AttributeProxy(null, (Attribute<?, ?>) node, locale);
		} else if (node instanceof Entity) {
			return new EntityProxy(null, (Entity) node, locale);
		}
		return null;
	}
	
	public NodeProxy(EntityProxy parent, Node<?> node, Locale locale) {
		super();
		this.node = node;
		this.locale = locale;
	}

	public static List<NodeProxy> fromList(EntityProxy parent,
			List<Node<?>> list, Locale locale) {
		List<NodeProxy> result = new ArrayList<NodeProxy>();
		if(list != null) {
			for (Node<?> node : list) {
				NodeProxy proxy;
				if(node instanceof Attribute<?, ?>) {
					if(node instanceof CodeAttribute) {
						proxy = new CodeAttributeProxy(parent, (CodeAttribute) node, locale);
					} else {
						proxy = new AttributeProxy(parent, (Attribute<?, ?>) node, locale);
					}
				} else {
					proxy = new EntityProxy(parent, (Entity) node, locale);
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
	public Integer getDefinitionId() {
		if(node.getDefinition() == null) {
			return null;
		} else {
			return node.getDefinition().getId();
		}
	}
	
	@ExternalizedProperty
	public Integer getParentId() {
		if(node.getParent() == null) {
			return null;
		} else {
			return node.getParent().getInternalId();
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

	protected Locale getLocale() {
		return locale;
	}
}
