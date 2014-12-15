/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;

/**
 * @author S. Ricci
 *
 */
public class BooleanAttributeDefinitionFormObject<T extends BooleanAttributeDefinition> extends AttributeDefinitionFormObject<T> {
	
	public static final String TYPE_FIELD = "typeValue";

	public enum Type {
		THREE_STATE, AFFIRMATIVE_ONLY
	}
	
	private String typeValue;
	
	BooleanAttributeDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
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
