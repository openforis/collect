/**
 * 
 */
package org.openforis.idm.model.expression.internal;

import java.util.Collection;
import java.util.Locale;

import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.NodePointerFactory;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.apache.commons.jxpath.util.ValueUtils;

/**
 * @author M. Togna
 * 
 */
public class ModelNodePointerFactory implements NodePointerFactory {

	public ModelNodePointerFactory() {
		super();
	}

	public int getOrder() {
		return 1;
	}

	public NodePointer createNodePointer(QName name, Object bean, Locale locale) {
		DynamicPropertyHandler propertyHandler = determineDynamicPropertyHandler(bean);
		if (propertyHandler == null) {
			Object obj = getHeadElement(bean);
			if (obj == null) {
				return null;
			} else {
				return createNodePointer(name, obj, locale);
			}
		} else {
			return new ModelNodePointer(name, bean, propertyHandler, locale);
		}
	}

	public NodePointer createNodePointer(NodePointer parent, QName name, Object bean) {
		if (bean == null) {
			return new NullPointer(parent, name);
		}
		DynamicPropertyHandler propertyHandler = determineDynamicPropertyHandler(bean);
		if (propertyHandler == null) {
			Object obj = getHeadElement(bean);
			if (obj == null) {
				return null;
			} else {
				return createNodePointer(parent, name, obj);
			}
		} else {
			return new ModelNodePointer(parent, name, bean, propertyHandler);
		}
	}

	private DynamicPropertyHandler determineDynamicPropertyHandler(Object bean) {
		JXPathBeanInfo bi = JXPathIntrospector.getBeanInfo(bean.getClass());
		if (bi.isDynamic()) {
			DynamicPropertyHandler handler = ValueUtils.getDynamicPropertyHandler(bi.getDynamicPropertyHandlerClass());
			return handler;
		} else {
			return null;
		}
	}

	private Object getHeadElement(Object bean) {
		if (bean instanceof Collection<?>) {
			Collection<?> collection = (Collection<?>) bean;
			if (collection.size() == 1) {
				return collection.iterator().next();
			}
		}
		return null;
	}

}
