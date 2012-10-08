/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.BooleanAttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public class BooleanAttributeDefinitionFormObject<T extends BooleanAttributeDefinition> extends AttributeDefinitionFormObject<T> {
	
	enum Type {
		THREE_STATE, AFFIRMATIVE_ONLY
	}
	
	private String typeValue;
	
	public BooleanAttributeDefinitionFormObject() {
		typeValue = Type.THREE_STATE.name();
	}
	
	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		boolean affirmativeOnly = false;
		if ( typeValue != null ) {
			Type type = Type.valueOf(typeValue);
			if ( type == Type.AFFIRMATIVE_ONLY ) {
				affirmativeOnly = true;
			}
		}
		dest.setAffirmativeOnly(affirmativeOnly);
	}
	
	@Override
	public void loadFrom(T source, String languageCode) {
		super.loadFrom(source, languageCode);
		typeValue = source.isAffirmativeOnly() ? Type.AFFIRMATIVE_ONLY.name(): Type.THREE_STATE.name();
	}

	public String getTypeValue() {
		return typeValue;
	}

	public void setTypeValue(String typeValue) {
		this.typeValue = typeValue;
	}

}
