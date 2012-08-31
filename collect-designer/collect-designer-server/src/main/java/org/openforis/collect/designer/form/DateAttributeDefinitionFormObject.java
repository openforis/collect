/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.CodeAttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public class DateAttributeDefinitionFormObject<T extends CodeAttributeDefinition> extends AttributeDefinitionFormObject<T> {
	
	@Override
	public void copyValues(T dest, String languageCode) {
		super.copyValues(dest, languageCode);
	}
	
	@Override
	public void setValues(T source, String languageCode) {
		super.setValues(source, languageCode);
	}

}
