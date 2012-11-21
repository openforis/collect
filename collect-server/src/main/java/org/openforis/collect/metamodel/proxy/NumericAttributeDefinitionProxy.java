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
public abstract class NumericAttributeDefinitionProxy extends AttributeDefinitionProxy {

	private transient NumericAttributeDefinition numericAttributeDefinition;

	public enum Type {
		INTEGER, REAL
	}
	
	public NumericAttributeDefinitionProxy(EntityDefinitionProxy parent, NumericAttributeDefinition attributeDefinition) {
		super(parent, attributeDefinition);
		this.numericAttributeDefinition = attributeDefinition;
	}
	
	@ExternalizedProperty
	public Type getType() {
		if (numericAttributeDefinition.getType() != null) {
			return Type.valueOf(numericAttributeDefinition.getType().toString());
		} else {
			return null;
		}
	}

	@ExternalizedProperty
	public boolean isInteger() {
		return numericAttributeDefinition.isInteger();
	}

	@ExternalizedProperty
	public boolean isReal() {
		return numericAttributeDefinition.isReal();
	}

	@ExternalizedProperty
	public List<PrecisionProxy> getPrecisionDefinitions() {
		return PrecisionProxy.fromList(numericAttributeDefinition.getPrecisionDefinitions());
	}
	
	
}
