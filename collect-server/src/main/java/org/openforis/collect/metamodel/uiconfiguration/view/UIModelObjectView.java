package org.openforis.collect.metamodel.uiconfiguration.view;

import org.openforis.collect.metamodel.ui.UIModelObject;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.NodeDefinition;

public abstract class UIModelObjectView<O extends UIModelObject> {
	
	protected O uiObject;

	public UIModelObjectView(O uiObject) {
		super();
		this.uiObject = uiObject;
	}
	
	public abstract String getType();

	protected NodeDefinition getNodeDefinition(int defId) {
		CollectSurvey survey = getSurvey();
		NodeDefinition def = survey.getSchema().getDefinitionById(defId);
		return def;
	}

	protected CollectSurvey getSurvey() {
		CollectSurvey survey = uiObject.getUIConfiguration().getSurvey();
		return survey;
	}

	public int getId() {
		return uiObject.getId();
	}
}