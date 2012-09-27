/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.RangeAttributeDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class RangeAttributeDefinitionFormObject<T extends RangeAttributeDefinition> extends NumericAttributeDefinitionFormObject<T> {

	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
	}
	
	public void loadFrom(T source, String languageCode) {
		super.loadFrom(source, languageCode);
	}

}
