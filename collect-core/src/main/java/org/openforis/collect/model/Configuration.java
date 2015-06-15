package org.openforis.collect.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * @author S. Ricci
 */
public class Configuration implements Cloneable {

	public enum ConfigurationItem {
		RECORD_FILE_UPLOAD_PATH("upload_path"),
		RECORD_INDEX_PATH("index_path"),
		BACKUP_STORAGE_PATH("backup_path"),
		RESTORED_BACKUP_STORAGE_PATH("restored_backup_path");
		
		private String key;
		
		public static ConfigurationItem fromKey(String key) {
			ConfigurationItem[] values = values();
			for (ConfigurationItem configurationItem : values) {
				if (configurationItem.key.equals(key)) {
					return configurationItem;
				}
			}
			throw new IllegalArgumentException("Invalid Configuration Key specified: " + key);
		}
		
		ConfigurationItem(String key) {
			this.key = key;
		}
		
		public String getKey() {
			return key;
		}
	}
	
	private Map<ConfigurationItem, String> map;
	
	public Configuration() {
		map = new HashMap<ConfigurationItem, String>();
	}
	
	public Configuration(Map<String, String> map) {
		map.putAll(map);
	}
	
	public String getUploadPath() {
		return get(ConfigurationItem.RECORD_FILE_UPLOAD_PATH);
	}
	
	public void setUploadPath(String path) {
		put(ConfigurationItem.RECORD_FILE_UPLOAD_PATH, path);
	}
	
	public String getIndexPath() {
		return get(ConfigurationItem.RECORD_INDEX_PATH);
	}
	
	public void setIndexPath(String path) {
		put(ConfigurationItem.RECORD_INDEX_PATH, path);
	}
	
	public String getBackupStoragePath() {
		return get(ConfigurationItem.BACKUP_STORAGE_PATH);
	}
	
	public void setBackupStoragePath(String path) {
		put(ConfigurationItem.BACKUP_STORAGE_PATH, path);
	}
	
	public String get(ConfigurationItem configurationItem) {
		return map.get(configurationItem);
	}
	
	public void put(ConfigurationItem configurationItem, String value) {
		value = StringUtils.trimToNull(value);
		if ( value == null ) {
			map.remove(configurationItem);
		} else {
			map.put(configurationItem,  value);
		}
	}
	
	public Set<ConfigurationItem> getProperties() {
		return map.keySet();
	}
	
	public Object clone() throws CloneNotSupportedException {
		Configuration conf = (Configuration) super.clone();
		if ( this.map != null ) {
			conf.map = new HashMap<ConfigurationItem, String>(this.map);
		}
		return conf;
	}

}
