/**
 * 
 */
package org.openforis.idm.metamodel;

import java.io.Serializable;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class LanguageSpecificText implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	private String language;
	private String text;

	public LanguageSpecificText(String language, String text) {
		this.language = language;
		this.text = text;
	}

	public String getLanguage() {
		return this.language;
	}

	public String getText() {
		return this.text;
	}
	
	protected void setText(String text) {
		this.text = text;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((language == null) ? 0 : language.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LanguageSpecificText other = (LanguageSpecificText) obj;
		if (language == null) {
			if (other.language != null)
				return false;
		} else if (!language.equals(other.language))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("%s [%s]", text, language);
	}
	
	@Override
	public LanguageSpecificText clone() throws CloneNotSupportedException {
		return (LanguageSpecificText) super.clone();
	}
	
}
