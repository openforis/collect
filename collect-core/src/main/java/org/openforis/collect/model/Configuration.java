package org.openforis.collect.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author S. Ricci
 */
public class Configuration {

	private Map<String, String> map;
	
	public Configuration() {
		map = new HashMap<String, String>();
	}
	
	public String put(String propertyName, String value) {
		return map.put(propertyName, value);
	}

	public String get(String propertyName) {
		return map.get(propertyName);
	}

	public Set<String> getProperties() {
		return map.keySet();
	}
}
