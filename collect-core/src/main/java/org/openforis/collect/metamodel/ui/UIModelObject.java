package org.openforis.collect.metamodel.ui;

import java.io.Serializable;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class UIModelObject implements Serializable, Identifiable {

	private static final long serialVersionUID = 1L;
	
	private UIModelObject parent;
	private int id;

	UIModelObject(UIModelObject parent, int id) {
		super();
		this.parent = parent;
		this.id = id;
	}
	
	protected NodeDefinition getNodeDefinition(int id) {
		UIConfiguration uiConfiguration = getUIConfiguration();
		if ( uiConfiguration == null || uiConfiguration.getSurvey() == null ) {
			throw new IllegalStateException("UIConfiguration not initialized correctly");
		}
		CollectSurvey survey = uiConfiguration.getSurvey();
		Schema schema = survey.getSchema();
		NodeDefinition result = schema.getDefinitionById(id);
		return result;
	}
	
	public FormSet getFormSet() {
		UIModelObject currentObject = this;
		while ( currentObject.getParent() != null ) {
			currentObject = currentObject.getParent();
		}
		return (FormSet) currentObject;
	}
	
	public UIConfiguration getUIConfiguration() {
		FormSet formSet = getFormSet();
		return formSet.getUIConfiguration();
	}
	
	public UIModelObject getParent() {
		return parent;
	}

	@Override
	public int getId() {
		return id;
	}
}
