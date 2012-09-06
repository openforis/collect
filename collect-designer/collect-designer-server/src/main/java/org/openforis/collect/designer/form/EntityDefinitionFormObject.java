/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.ui.UIConfiguration;
import org.openforis.collect.model.ui.UITabDefinition;
import org.openforis.idm.metamodel.EntityDefinition;

/**
 * @author S. Ricci
 *
 */
public class EntityDefinitionFormObject<T extends EntityDefinition> extends NodeDefinitionFormObject<T> {

	private UITabDefinition tabDefinition;
	
	public void copyValues(T dest, String languageCode) {
		super.copyValues(dest, languageCode);
		if ( dest.getParentDefinition() == null ) {
			//root entity
			String tabDefinitionName = null;
			if ( tabDefinition != null ) {
				tabDefinitionName = tabDefinition.getName();
			}
			dest.setAnnotation(UIConfiguration.TAB_DEFINITION_ANNOTATION, tabDefinitionName);
		}
	}
	
	public void setValues(T source, String languageCode) {
		super.setValues(source, languageCode);
		if ( source.getParentDefinition() == null ) {
			//root entity
			String tabDefinitionName = source.getAnnotation(UIConfiguration.TAB_DEFINITION_ANNOTATION);
			CollectSurvey survey = (CollectSurvey) source.getSurvey();
			UIConfiguration uiConfig = survey.getUIConfiguration();
			tabDefinition = uiConfig.getTabDefinition(tabDefinitionName);
		}
	}

	public UITabDefinition getTabDefinition() {
		return tabDefinition;
	}
	
	public void setTabDefinition(UITabDefinition tabDefinition) {
		this.tabDefinition = tabDefinition;
	}
}
