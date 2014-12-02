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
	
}
