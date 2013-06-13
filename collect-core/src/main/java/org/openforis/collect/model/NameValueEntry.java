package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

/**
 * 
 * @author S. Ricci
 *
 */
public class NameValueEntry extends SimpleEntry<String, Object> {

	private static final long serialVersionUID = 1L;
	
	public NameValueEntry(Entry<? extends String, ? extends Object> entry) {
		super(entry);
	}
	
	public NameValueEntry(String key, Object value) {
		super(key, value);
	}
	
	public static NameValueEntry[] fromKeyValuePairs(Object... keys) {
		if ( keys == null || keys.length % 2 == 1 ) {
			throw new IllegalArgumentException("Invalid keys specified: odd couple of values expected");
		}
		List<NameValueEntry> result = new ArrayList<NameValueEntry>();
		for(int i = 0; i < keys.length; i+=2 ) {
			String key = (String) keys[i];
			String value = (String) keys[i+1];
			NameValueEntry pair = new NameValueEntry(key, value);
			result.add(pair);
		}
		return result.toArray(new NameValueEntry[0]);
	}

}