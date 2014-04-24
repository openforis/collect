package org.openforis.collect.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author S. Ricci
 */
public class Configuration implements Cloneable {

	public static final String UPLOAD_PATH_KEY = "upload_path";
	public static final String INDEX_PATH_KEY = "index_path";
	public static final String[] SUPPORTED_KEYS = new String[]{UPLOAD_PATH_KEY, INDEX_PATH_KEY};
	
	private Map<String, String> map;
	
	public Configuration() {
		map = new HashMap<String, String>();
	}
	
	public Configuration(Map<String, String> map) {
		map.putAll(map);
	}
	
	public String getUploadPath() {
		return map.get(UPLOAD_PATH_KEY);
	}
	
	public void setUploadPath(String path) {
		put(UPLOAD_PATH_KEY, path);
	}
	
	public String getIndexPath() {
		return map.get(INDEX_PATH_KEY);
	}
	
	public void setIndexPath(String path) {
		put(INDEX_PATH_KEY, path);
	}
	
	public String get(String key) {
		checkSupported(key);
		return map.get(key);
	}
	
	public void put(String key, String value) {
		checkSupported(key);
		if ( StringUtils.isBlank(value) ) {
			map.remove(key);
		} else {
			map.put(key,  value);
		}
	}
	
	private boolean checkSupported(String key) {
		if ( ArrayUtils.contains(SUPPORTED_KEYS, key) ) {
			return true;
		} else {
			throw new IllegalArgumentException("Unsupported configuration property: " + key);
		}
	}
	
	public Set<String> getProperties() {
		return map.keySet();
	}
	
	public Object clone() throws CloneNotSupportedException {
		Configuration conf = (Configuration) super.clone();
		if ( this.map != null ) {
			conf.map = new HashMap<String, String>(this.map);
		}
		return conf;
	}
}
