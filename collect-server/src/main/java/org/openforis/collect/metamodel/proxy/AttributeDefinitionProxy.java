/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import org.apache.commons.lang3.StringUtils;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.metamodel.CollectAnnotations.Annotation;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.KeyAttributeDefinition;

/**
 * @author M. Togna
 * 
 */
public abstract class AttributeDefinitionProxy extends NodeDefinitionProxy implements Proxy {

	private transient AttributeDefinition attributeDefinition;

	public AttributeDefinitionProxy(EntityDefinitionProxy parent, AttributeDefinition attributeDefinition) {
		super(parent, attributeDefinition);
		this.attributeDefinition = attributeDefinition;
	}

	@ExternalizedProperty
	public boolean isKey() {
		if(this.attributeDefinition instanceof KeyAttributeDefinition) {
			return ((KeyAttributeDefinition) this.attributeDefinition).isKey(); 
		} else {
			return false;
		}
	}
	
	@ExternalizedProperty
	public boolean isDefaultValueApplicable() {
		return ! attributeDefinition.getAttributeDefaults().isEmpty();
	}
	
	@ExternalizedProperty
	public boolean isAutocomplete() {
		String autocompleteStrValue = attributeDefinition.getAnnotation(Annotation.AUTOCOMPLETE.getQName());
		boolean autocomplete = StringUtils.isNotBlank(autocompleteStrValue);
		return autocomplete;
	}
	
	@ExternalizedProperty
	public String[] getVisibleFields() {
		CollectSurvey survey = (CollectSurvey) attributeDefinition.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		String[] result = uiOptions.getVisibleFields(attributeDefinition);
		return result;
	}
	
	protected boolean isFieldVisible(String field) {
		CollectSurvey survey = (CollectSurvey) attributeDefinition.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		return uiOptions.isVisibleField(attributeDefinition, field);
	}
	
}
