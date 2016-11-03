/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import java.util.List;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Value;

/**
 * @author S. Ricci
 */
public class EntityKeyValidator implements ValidationRule<Attribute<?, ?>> {

	@Override
	public ValidationResultFlag evaluate(Attribute<?, ?> keyAttribute) {
		Entity multipleEntity = keyAttribute.getNearestMultipleEntityAncestor();
		
		EntityDefinition multipleEntityDef = multipleEntity.getDefinition();
		
		if ( multipleEntityDef.isRoot() ) {
			return ValidationResultFlag.OK;
		}
		Value[] keyValues = multipleEntity.getKeyAttributeValues();
		if (keyValues == null) {
			return null;
		}
		List<Entity> entities = multipleEntity.getParent().findChildEntitiesByKeys(multipleEntityDef, keyValues);
		
		if ( entities.size() > 1 ) {
			return ValidationResultFlag.ERROR;
		} else {
			return ValidationResultFlag.OK;
		}
	}

}
