package org.openforis.collect.datacleansing.json;

import java.util.List;

import org.json.simple.JSONObject;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Field;

/**
 * 
 * @author S. Ricci
 *
 */
public class JSONValueFormatter {

	@SuppressWarnings("unchecked")
	public String formatValue(Attribute<?, ?> attr) {
		if (attr.isEmpty()) {
			return null;
		}
		JSONObject jsonObj = new JSONObject();
		List<Field<?>> fields = attr.getFields();
		for (Field<?> field : fields) {
			jsonObj.put(field.getName(), field.getValue());
		}
		return jsonObj.toJSONString();
	}
	
//	@SuppressWarnings("unchecked")
//	public String format(Value value) {
//		JSONObject jsonObj = new JSONObject();
//		Map<String, Object> map = value.toMap();
//		Set<Entry<String,Object>> entrySet = map.entrySet();
//		for (Entry<String, Object> entry : entrySet) {
//			Object val = entry.getValue();
//			if (val != null && ! (val instanceof String && StringUtils.isBlank((String) val))) {
//				jsonObj.put(entry.getKey(), val);
//			}
//		}
//		return jsonObj.toJSONString();
//	}
}
