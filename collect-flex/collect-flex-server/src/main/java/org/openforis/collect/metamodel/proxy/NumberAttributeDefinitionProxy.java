/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.NumericAttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public class NumberAttributeDefinitionProxy extends AttributeDefinitionProxy {

	private transient NumericAttributeDefinition attributeDefinition;

	public enum Type {
		INTEGER, REAL
	}
	
	public NumberAttributeDefinitionProxy(EntityDefinitionProxy parent, NumericAttributeDefinition attributeDefinition) {
		super(parent, attributeDefinition);
		this.attributeDefinition = attributeDefinition;
	}
	
	@ExternalizedProperty
	public Type getType() {
		if (attributeDefinition.getType() != null) {
			return Type.valueOf(attributeDefinition.getType().toString());
		} else {
			return null;
		}
	}

	@ExternalizedProperty
	public boolean isInteger() {
		return attributeDefinition.isInteger();
	}

	@ExternalizedProperty
	public boolean isReal() {
		return attributeDefinition.isReal();
	}

	@ExternalizedProperty
	public List<PrecisionProxy> getPrecisionDefinitions() {
		return PrecisionProxy.fromList(attributeDefinition.getPrecisionDefinitions());
	}
	
	
}
