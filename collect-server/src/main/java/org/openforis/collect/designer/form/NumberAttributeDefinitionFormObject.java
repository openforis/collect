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

	private boolean key;

	NumberAttributeDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
		key = false;
	}

	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setKey(key);
	}
	
	@Override
	public void loadFrom(T source, String languageCode, String defaultLanguage) {
		super.loadFrom(source, languageCode, defaultLanguage);
		key = source.isKey();
	}

	public boolean isKey() {
		return key;
	}

	public void setKey(boolean key) {
		this.key = key;
	}
	
}
