/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition.Type;

/**
 * @author S. Ricci
 *
 */
public class TextAttributeDefinitionFormObject<T extends TextAttributeDefinition> extends AttributeDefinitionFormObject<T> {
	
	private boolean key;
	private String type;
	
	public TextAttributeDefinitionFormObject() {
		Type.SHORT.name();
	}
	
	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setKey(key);
		Type typeEnum = TextAttributeDefinition.Type.valueOf(type);
		dest.setType(typeEnum);
	}
	
	@Override
	public void loadFrom(T source, String languageCode) {
		super.loadFrom(source, languageCode);
		key = source.isKey();
		Type typeEnum = source.getType();
		if ( typeEnum == null ) {
			typeEnum = Type.SHORT;
		}
		type = typeEnum.name();
	}


	public boolean isKey() {
		return key;
	}

	public void setKey(boolean key) {
		this.key = key;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
