package org.openforis.idm.metamodel;

import javax.xml.bind.annotation.XmlAttribute;


/**
 * 
 * @author S. Ricci
 *
 */
public class TypedLanguageSpecificText<T extends Object> extends LanguageSpecificText {

	private static final long serialVersionUID = 1L;
	
	@XmlAttribute(name = "type")
	private T type;

	public TypedLanguageSpecificText(T type, String language, String text) {
		super(language, text);
		this.type = type;
	}
	
	public T getType() {
		return this.type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypedLanguageSpecificText<?> other = (TypedLanguageSpecificText<?>) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	
}
