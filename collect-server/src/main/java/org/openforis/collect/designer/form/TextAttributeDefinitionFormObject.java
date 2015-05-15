/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.collect.metamodel.CollectAnnotations.Annotation;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition.Type;

/**
 * @author S. Ricci
 *
 */
public class TextAttributeDefinitionFormObject<T extends TextAttributeDefinition> extends AttributeDefinitionFormObject<T> {

	private String type;
	private String autocompleteGroup;
	private boolean autoUppercase;
	
	TextAttributeDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
		Type.SHORT.name();
	}
	
	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		Type typeEnum = TextAttributeDefinition.Type.valueOf(type);
		dest.setType(typeEnum);
		dest.setAnnotation(Annotation.AUTOCOMPLETE.getQName(), autocompleteGroup);
		
		UIOptions uiOptions = getUIOptions(dest);
		uiOptions.setAutoUppercase(dest, autoUppercase);
	}
	
	@Override
	public void loadFrom(T source, String languageCode) {
		super.loadFrom(source, languageCode);
		Type typeEnum = source.getType();
		if ( typeEnum == null ) {
			typeEnum = Type.SHORT;
		}
		type = typeEnum.name();
		autocompleteGroup = source.getAnnotation(Annotation.AUTOCOMPLETE.getQName());
		
		UIOptions uiOptions = getUIOptions(source);
		autoUppercase = uiOptions.isAutoUppercase(source);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAutocompleteGroup() {
		return autocompleteGroup;
	}

	public void setAutocompleteGroup(String autocompleteGroup) {
		this.autocompleteGroup = autocompleteGroup;
	}
	
	public boolean isAutoUppercase() {
		return autoUppercase;
	}
	
	public void setAutoUppercase(boolean autoUppercase) {
		this.autoUppercase = autoUppercase;
	}
}
