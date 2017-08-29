/**
 * 
 */
package org.openforis.idm.metamodel;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.commons.collection.CollectionUtils;

/**
 * @author S. Ricci
 *
 */
public abstract class LanguageSpecificTextAbstractMap<T extends LanguageSpecificText> implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private static final String VOID_LANGUAGE_KEY = "";

	private final Class<T> genericType;
	
	LinkedHashMap<String, T> map;

	@SuppressWarnings("unchecked")
	LanguageSpecificTextAbstractMap() {
		ParameterizedType parameterizedType = (ParameterizedType)getClass().getGenericSuperclass();
		Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
		genericType = (Class<T>) actualTypeArguments[0];
		map = new LinkedHashMap<String, T>();
	}
	
	LanguageSpecificTextAbstractMap(LanguageSpecificTextAbstractMap<T> obj) {
		this();
		CollectionUtils.cloneValuesInto(obj.map, this.map);
	}

	public T get(String language) {
		String key = getMapKey(language);
		return map.get(key);
	}

	protected String getMapKey(String language) {
		String key = language == null ? VOID_LANGUAGE_KEY: language;
		return key;
	}
	
	public List<T> values() {
		Collection<T> result = map.values();
		return CollectionUtils.unmodifiableList(new ArrayList<T>(result));
	}
	
	public boolean hasText(String language) {
		return map.containsKey(getMapKey(language));
	}
	
	public String getText(String language) {
		T languageSpecificText = get(language);
		return languageSpecificText != null ? languageSpecificText.getText(): null;
	}
	
	public void setText(String language, String text) {
		if ( StringUtils.isBlank(text) ) {
			remove(language);
		} else {
			String key = getMapKey(language);
			T languageSpecificText = map.get(key);
			if ( languageSpecificText == null ) {
				languageSpecificText = createLanguageSpecificTextInstance(language, text);
				map.put(key, languageSpecificText);
			} else {
				languageSpecificText.setText(text);
			}	
		}
	}

	public void add(T languageSpecificText) {
		String key = getMapKey(languageSpecificText.getLanguage());
		map.put(key, languageSpecificText);
	}
	
	public void remove(String language) {
		String key = getMapKey(language);
		map.remove(key);
	}
	
	public void removeAll() {
		map.clear();
	}

	protected T createLanguageSpecificTextInstance(String language, String text) {
		try {
			T instance = genericType.getConstructor(String.class, String.class).newInstance(language, text);
			return instance;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((map == null) ? 0 : map.hashCode());
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
		LanguageSpecificTextAbstractMap<?> other = (LanguageSpecificTextAbstractMap<?>) obj;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return map == null ? null: map.toString();
	}
	
}
