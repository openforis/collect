/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition.Type;

/**
 * @author S. Ricci
 *
 */
public class NumericAttributeDefinitionFormObject<T extends NumericAttributeDefinition> extends AttributeDefinitionFormObject<T> {
	
	private String type;
	
	NumericAttributeDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
		type = NumericAttributeDefinition.Type.INTEGER.name();
	}
	
	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		Type typeEnum = null;
		if ( type != null ) {
			typeEnum = NumericAttributeDefinition.Type.valueOf(type);
		}
		dest.setType(typeEnum);
	}

	@Override
	public void loadFrom(T source, String languageCode) {
		super.loadFrom(source, languageCode);
		type = source.getType() != null ? source.getType().name(): null;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
