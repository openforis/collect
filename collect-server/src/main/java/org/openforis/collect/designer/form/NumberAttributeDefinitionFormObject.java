/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class NumberAttributeDefinitionFormObject<T extends NumberAttributeDefinition> extends NumericAttributeDefinitionFormObject<T> {

	NumberAttributeDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
	}
	
}
