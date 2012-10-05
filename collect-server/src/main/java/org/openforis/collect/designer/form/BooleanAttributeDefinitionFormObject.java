/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.collect.model.ui.UIConfiguration;
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
		String annotationValue = null;
		if ( typeValue != null ) {
			Type type = Type.valueOf(typeValue);
			if ( type == Type.AFFIRMATIVE_ONLY ) {
				annotationValue = type.name().toLowerCase();
			}
		}
		dest.setAnnotation(UIConfiguration.Annotation.TYPE.getQName(), annotationValue);
	}
	
	@Override
	public void loadFrom(T source, String languageCode) {
		super.loadFrom(source, languageCode);
		typeValue = source.getAnnotation(UIConfiguration.Annotation.TYPE.getQName());
	}

	public String getTypeValue() {
		return typeValue;
	}

	public void setTypeValue(String typeValue) {
		this.typeValue = typeValue;
	}

}
