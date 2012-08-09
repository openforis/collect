package org.openforis.collect.designer.composer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.ModelVersion;
import org.zkoss.bind.BindComposer;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveyEditVersioningComposer extends BindComposer<Component> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@WireVariable
	private SurveyManager surveyManager;
	
	private List<ModelVersion> versions;
	
	private Map<ModelVersion, Boolean> editingStatus;

	public SurveyEditVersioningComposer() {
		editingStatus = new HashMap<ModelVersion, Boolean>();
	}
	
	@Command
	public void changeEditableStatus(@BindingParam("currentVersion") ModelVersion version) {
		Boolean editable = editingStatus.get(version);
		editingStatus.put(version, editable == null ? Boolean.TRUE : new Boolean(! editable.booleanValue()));
		refreshRowTemplate(version);
	}

	@Command
	public void confirm(@BindingParam("version") ModelVersion version) {
		changeEditableStatus(version);
	}

	public void refreshRowTemplate(ModelVersion version) {
		/*
		 * This code is special and notifies ZK that the bean's value
		 * has changed as it is used in the template mechanism.
		 * This stops the entire Grid's data from being refreshed
		 */
		BindUtils.postNotifyChange(null, null, this, "editingStatus");
	}

	public List<ModelVersion> getVersions() {
		if ( versions == null ) {
			CollectSurvey survey = surveyManager.get("naforma1");
			versions = survey.getVersions();
		}
		return versions;
	}

	public void setVersions(List<ModelVersion> versions) {
		this.versions = versions;
	}

	public Map<ModelVersion, Boolean> getEditingStatus() {
		return editingStatus;
	}
}
