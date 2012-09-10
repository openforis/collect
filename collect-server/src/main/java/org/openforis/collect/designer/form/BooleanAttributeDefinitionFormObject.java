/**
 * 
 */
package org.openforis.collect.designer.form;

import javax.xml.namespace.QName;

import org.openforis.idm.metamodel.BooleanAttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public class BooleanAttributeDefinitionFormObject<T extends BooleanAttributeDefinition> extends AttributeDefinitionFormObject<T> {
	
	private static final QName AFFIRMATIVE_ONLY_ANNOTATION = new QName("http://www.openforis.org/collect/3.0/ui", "type");
	
	enum Type {
		THREE_STATE, AFFIRMATIVE_ONLY
	}
	
	private Integer typeIndex;
	
	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		String annotationValue = null;
		if ( typeIndex != null ) {
			Type type = Type.values()[typeIndex];
			if ( type == Type.AFFIRMATIVE_ONLY ) {
				annotationValue = "true";
			}
		}
		dest.setAnnotation(AFFIRMATIVE_ONLY_ANNOTATION, annotationValue);
	}
	
	@Override
	public void loadFrom(T source, String languageCode) {
		super.loadFrom(source, languageCode);
		String affirmativeOnlyStringValue = source.getAnnotation(AFFIRMATIVE_ONLY_ANNOTATION);
		boolean affirmativeOnly = Boolean.parseBoolean(affirmativeOnlyStringValue);
		typeIndex = affirmativeOnly ? 1: 0;
	}

	public Integer getTypeIndex() {
		return typeIndex;
	}

	public void setTypeIndex(Integer typeIndex) {
		this.typeIndex = typeIndex;
	}

}
