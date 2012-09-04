/**
 * 
 */
package org.openforis.collect.designer.form;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class AttributeDefinitionFormObject<T extends AttributeDefinition> extends NodeDefinitionFormObject<T> {

	private List<AttributeDefault> attributeDefaults;

	public void copyValues(T dest, String languageCode) {
		super.copyValues(dest, languageCode);
		dest.removeAllAttributeDefaults();
		if ( attributeDefaults != null ) {
			for (AttributeDefault attrDefault : attributeDefaults) {
				dest.addAttributeDefault(attrDefault);
			}
		}
	}
	
	public void setValues(T source, String languageCode) {
		super.setValues(source, languageCode);
		attributeDefaults = new ArrayList<AttributeDefault>(source.getAttributeDefaults());
	}

	public List<AttributeDefault> getAttributeDefaults() {
		return attributeDefaults;
	}
	
	public void setAttributeDefaults(List<AttributeDefault> attributeDefaults) {
		this.attributeDefaults = attributeDefaults;
	}
	
}
