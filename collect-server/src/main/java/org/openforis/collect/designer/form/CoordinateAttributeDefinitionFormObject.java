/**
 * 
 */
package org.openforis.collect.designer.form;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.CoordinateAttributeFieldsOrder;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;

/**
 * @author S. Ricci
 *
 */
public class CoordinateAttributeDefinitionFormObject<T extends CoordinateAttributeDefinition> extends AttributeDefinitionFormObject<T> {
	
	private String fieldsOrderValue;
	
	CoordinateAttributeDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
	}

	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		saveFieldOrderValue(dest);
	}

	protected void saveFieldOrderValue(T dest) {
		CollectSurvey survey = (CollectSurvey) dest.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		CoordinateAttributeFieldsOrder fieldsOrder;
		if ( StringUtils.isBlank(fieldsOrderValue) ) {
			fieldsOrder = null;
		} else {
			fieldsOrder = CoordinateAttributeFieldsOrder.valueOf(fieldsOrderValue);
		}
		uiOptions.setFieldsOrder(dest, fieldsOrder);
	}
	
	@Override
	public void loadFrom(T source, String languageCode, String defaultLanguage) {
		super.loadFrom(source, languageCode, defaultLanguage);
		loadFieldsOrderValue(source);
	}

	protected void loadFieldsOrderValue(T source) {
		CollectSurvey survey = (CollectSurvey) source.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		CoordinateAttributeFieldsOrder fieldsOrder = uiOptions.getFieldsOrder(source);
		fieldsOrderValue = fieldsOrder.name();
	}

	public String getFieldsOrderValue() {
		return fieldsOrderValue;
	}

	public void setFieldsOrderValue(String fieldsOrderValue) {
		this.fieldsOrderValue = fieldsOrderValue;
	};

}
