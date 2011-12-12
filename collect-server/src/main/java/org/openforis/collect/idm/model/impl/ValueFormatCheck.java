/**
 * 
 */
package org.openforis.collect.idm.model.impl;

import org.openforis.idm.metamodel.InvalidCheckException;
import org.openforis.idm.metamodel.ModelObjectDefinition;
import org.openforis.idm.model.ModelObject;

/**
 * @author M. Togna
 * 
 */
public class ValueFormatCheck {

	public boolean execute(ModelObject<? extends ModelObjectDefinition> modelObject) throws InvalidCheckException {
		if (modelObject instanceof AttributeImpl) {
			return ((AttributeImpl<?, ?>) modelObject).isValueFormatValid();
		}
		throw new InvalidCheckException(null, "Unable to check the value format");
	}

}
