package org.openforis.collect.metamodel.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.CoordinateAttributeFieldsOrder;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class CoordinateAttributeDefinitionProxy extends AttributeDefinitionProxy {

	public CoordinateAttributeDefinitionProxy(EntityDefinitionProxy parent, CoordinateAttributeDefinition attributeDefinition) {
		super(parent, attributeDefinition);
	}
	
	@ExternalizedProperty
	public CoordinateAttributeFieldsOrder getFieldsOrder() {
		CollectSurvey survey = (CollectSurvey) nodeDefinition.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		return uiOptions.getFieldsOrder((CoordinateAttributeDefinition) nodeDefinition);
	}

}
