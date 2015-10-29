/**
 * 
 */
package org.openforis.idm.model.expression.internal;

import java.util.Locale;

import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.dynamic.DynamicPointer;
import org.openforis.idm.model.Attribute;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class ModelNodePointer extends DynamicPointer {

	private static final long serialVersionUID = 1L;
	
	private boolean normalizeNumbers;
	
	protected ModelNodePointer(NodePointer parent, QName name, Object bean, DynamicPropertyHandler handler) {
		super(parent, name, bean, handler);
		normalizeNumbers = false;
	}

	protected ModelNodePointer(QName name, Object bean, DynamicPropertyHandler handler, Locale locale) {
		super(name, bean, handler, locale);
		normalizeNumbers = false;
	}

	@Override
	public Object getValue() {
		Object node = super.getValue();
		if (node instanceof Attribute) {
			return AttributeValueUtils.extractMainFieldValue((Attribute<?, ?>) node, normalizeNumbers);
		} else {
			return node;
		}
	}

	void setNormalizeNumbers(boolean normalizeNumbers) {
		this.normalizeNumbers = normalizeNumbers;
	}
	
	boolean isNormalizeNumbers() {
		return normalizeNumbers;
	}
}
