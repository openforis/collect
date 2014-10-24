/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import java.util.List;

import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;

/**
 * @author S. Ricci
 */
public class EntityKeyValidator implements ValidationRule<Attribute<?, ?>> {

	@Override
	public ValidationResultFlag evaluate(Attribute<?, ?> keyAttribute) {
		Entity multipleEntity = keyAttribute.getNearestAncestorMultipleEntity();
		
		if ( multipleEntity.getDefinition().isRoot() ) {
			return ValidationResultFlag.OK;
		} else {
			String[] keyValues = multipleEntity.getKeyValues();

			List<Entity> entities = multipleEntity.getParent().findChildEntitiesByKeys(multipleEntity.getName(), keyValues);
			
			if ( entities.size() > 1 ) {
				return ValidationResultFlag.ERROR;
			} else {
				return ValidationResultFlag.OK;
			}
		}
	}

}
