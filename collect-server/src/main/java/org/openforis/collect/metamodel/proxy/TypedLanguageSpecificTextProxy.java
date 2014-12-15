/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.metamodel.TypedLanguageSpecificText;

/**
 * @author S. Ricci
 * 
 */
public abstract class TypedLanguageSpecificTextProxy<T extends Object, P extends Object> implements Proxy {

	protected transient TypedLanguageSpecificText<T> typedLanguageSpecificText;

	public TypedLanguageSpecificTextProxy(TypedLanguageSpecificText<T> typedLanguageSpecificText) {
		super();
		this.typedLanguageSpecificText = typedLanguageSpecificText;
	}

	@ExternalizedProperty
	public String getLanguage() {
		return typedLanguageSpecificText.getLanguage();
	}

	@ExternalizedProperty
	public String getText() {
		return typedLanguageSpecificText.getText();
	}

	public T getTypeInternal() {
		return typedLanguageSpecificText.getType();
	}

}
