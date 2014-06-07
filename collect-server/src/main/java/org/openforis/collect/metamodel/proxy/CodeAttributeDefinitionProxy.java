/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;

/**
 * @author S. Ricci
 *
 */
public class CodeAttributeDefinitionProxy extends AttributeDefinitionProxy {

	private transient CodeAttributeDefinition attributeDefinition;

	public CodeAttributeDefinitionProxy(EntityDefinitionProxy parent, CodeAttributeDefinition attributeDefinition) {
		super(parent, attributeDefinition);
		this.attributeDefinition = attributeDefinition;
	}

	@ExternalizedProperty
	public String getListName() {
		return attributeDefinition.getListName();
	}

	@ExternalizedProperty
	public boolean isAllowUnlisted() {
		return attributeDefinition.isAllowUnlisted();
	}

	@ExternalizedProperty
	public boolean isExternal() {
		CodeList list = attributeDefinition.getList();
		return list != null && list.isExternal();
	}
	
	@ExternalizedProperty
	public boolean isAllowValuesSorting() {
		return attributeDefinition.isAllowValuesSorting();
	}

	@ExternalizedProperty
	public boolean isShowAllowedValuesPreview() {
		CollectSurvey survey = (CollectSurvey) attributeDefinition.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		return uiOptions.getShowAllowedValuesPreviewValue(attributeDefinition);
	}
}
