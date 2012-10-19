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

	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.removeAllAttributeDefaults();
		if ( attributeDefaults != null ) {
			for (AttributeDefault attrDefault : attributeDefaults) {
				dest.addAttributeDefault(attrDefault);
			}
		}
	}
	
	public void loadFrom(T source, String languageCode) {
		super.loadFrom(source, languageCode);
		attributeDefaults = new ArrayList<AttributeDefault>(source.getAttributeDefaults());
	}

	public List<AttributeDefault> getAttributeDefaults() {
		return attributeDefaults;
	}
	
	public void setAttributeDefaults(List<AttributeDefault> attributeDefaults) {
		this.attributeDefaults = attributeDefaults;
	}
	
}
