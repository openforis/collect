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

	public void copyValues(T dest, String languageCode) {
		super.copyValues(dest, languageCode);
		dest.setKey(key);
	}
	
	public void setValues(T source, String languageCode) {
		super.setValues(source, languageCode);
		key = source.isKey();
	}

	public boolean isKey() {
		return key;
	}

	public void setKey(boolean key) {
		this.key = key;
	}
	
}
