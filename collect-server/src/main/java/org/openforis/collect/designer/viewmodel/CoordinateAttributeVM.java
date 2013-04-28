/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.collect.metamodel.ui.UIOptions.CoordinateAttributeFieldsOrder;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;

/**
 * @author S. Ricci
 *
 */
public class CoordinateAttributeVM extends AttributeVM<CoordinateAttributeDefinition> {

	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentEntity") EntityDefinition parentEntity, 
			@ExecutionArgParam("item") CoordinateAttributeDefinition attributeDefn, 
			@ExecutionArgParam("newItem") Boolean newItem) {
		super.init(parentEntity, attributeDefn, newItem);
	}
	
	
	public String[] getFieldsOrderValues() {
		CoordinateAttributeFieldsOrder[] values = CoordinateAttributeFieldsOrder.values();
		String[] result = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			CoordinateAttributeFieldsOrder fieldsOrder = values[i];
			result[i] = fieldsOrder.name();
		}
		return result;
	}
	
	public String getFieldsOrderLabel(String value) {
		CoordinateAttributeFieldsOrder enumValue = CoordinateAttributeFieldsOrder.valueOf(value);
		String messageKey;
		switch (enumValue) {
		case SRS_X_Y:
			messageKey = "survey.schema.attribute.coordinate.fields_order.srs_x_y";
			break;
		case SRS_Y_X:
			messageKey = "survey.schema.attribute.coordinate.fields_order.srs_y_x";
			break;
		default:
			messageKey = enumValue.name();
		}
		return Labels.getLabel(messageKey);
	}
	
	
}
