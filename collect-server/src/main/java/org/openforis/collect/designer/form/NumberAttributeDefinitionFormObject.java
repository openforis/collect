/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.NumberAttributeDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class NumberAttributeDefinitionFormObject<T extends NumberAttributeDefinition> extends NumericAttributeDefinitionFormObject<T> {

	private boolean key;

	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setKey(key);
	}
	
	public void loadFrom(T source, String languageCode) {
		super.loadFrom(source, languageCode);
		key = source.isKey();
	}

	public boolean isKey() {
		return key;
	}

	public void setKey(boolean key) {
		this.key = key;
	}
	
}
