/**
 * 
 */
package org.openforis.collect.idm.model.impl;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Value;

/**
 * @author M. Togna
 * 
 */
public class AttributeImpl<D extends AttributeDefinition, V extends Value> extends AbstractModelObject<D> implements Attribute<D, V> {

	private V value;

	@Override
	public V getValue() {
		return this.value;
	}

	@Override
	public void setValue(V value) {
		this.value = value;
	}

}
