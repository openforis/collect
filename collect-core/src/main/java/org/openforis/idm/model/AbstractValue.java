/**
 * 
 */
package org.openforis.idm.model;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

/**
 * @author S. Ricci
 *
 */
public abstract class AbstractValue implements Value {

	public abstract Map<String, Object> toMap();

	public abstract String toPrettyFormatString();
	
	@SuppressWarnings("unchecked")
	public String toJSONString() {
		JSONObject jsonObj = new JSONObject();
		Map<String, Object> map = toMap();
		Set<Entry<String,Object>> entrySet = map.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			Object value = entry.getValue();
			if (value != null && ! (value instanceof String && StringUtils.isBlank((String) value))) {
				jsonObj.put(entry.getKey(), value);
			}
		}
		return jsonObj.toJSONString();
	}
	
	public String toString() {
		return toJSONString();
	}
	
}
