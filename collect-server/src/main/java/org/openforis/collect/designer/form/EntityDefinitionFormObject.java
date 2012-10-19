/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.EntityDefinition;

/**
 * @author S. Ricci
 *
 */
public class EntityDefinitionFormObject<T extends EntityDefinition> extends NodeDefinitionFormObject<T> {

	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
	}
	
	public void loadFrom(T source, String languageCode) {
		super.loadFrom(source, languageCode);
	}

}
