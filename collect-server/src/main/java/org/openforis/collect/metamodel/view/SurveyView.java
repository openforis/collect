package org.openforis.collect.metamodel.view;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.metamodel.ui.UIConfiguration;
import org.openforis.collect.metamodel.uiconfiguration.view.UIConfigurationView;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveyAvailability;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.persistence.xml.CeoApplicationOptions;

public class SurveyView {
	
	private Integer id;
	private String name;
	private String projectName;
	private String description;
	private boolean temporary;
	private SurveyTarget target;
	private UserGroup userGroup;
	private SchemaView schema;
	private List<CodeListView> codeLists = new ArrayList<CodeListView>();
	private CeoApplicationOptions ceoApplicationOptions;
	private UIConfiguration uiConfiguration;
	private SurveyAvailability availability;
	
	public SurveyView(Integer id, String name, boolean temporary, SurveyTarget target) {
		this.id = id;
		this.name = name;
		this.temporary = temporary;
		this.target = target;
	}
	
	public SurveyView(CollectSurvey survey) {
		this(survey.getId(), survey.getName(), survey.isTemporary(), survey.getTarget());
		this.projectName = survey.getProjectName();
		this.description = survey.getDescription();
		this.userGroup = survey.getUserGroup();
		this.schema = new SchemaView();
		this.uiConfiguration = survey.getUIConfiguration();
		this.ceoApplicationOptions = survey.getApplicationOptions(CeoApplicationOptions.TYPE);
		this.availability = survey.getAvailability();
	}

	public UIConfigurationView getUiConfiguration() {
		return new UIConfigurationView(uiConfiguration);
	}
	
	public void addCodeList(CodeListView codeListView) {
		this.codeLists.add(codeListView);
	}

	public Integer getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getProjectName() {
		return projectName;
	}
	
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public boolean isTemporary() {
		return temporary;
	}
	
	public void setTemporary(boolean temporary) {
		this.temporary = temporary;
	}
	
	public SurveyTarget getTarget() {
		return target;
	}
	
	public void setTarget(SurveyTarget target) {
		this.target = target;
	}
	
	public UserGroup getUserGroup() {
		return userGroup;
	}
	
	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = userGroup;
	}
	
	public List<CodeListView> getCodeLists() {
		return codeLists;
	}
	
	public SchemaView getSchema() {
		return schema;
	}
	
	public CeoApplicationOptions getCeoApplicationOptions() {
		return ceoApplicationOptions;
	}
	
	public void setCeoApplicationOptions(CeoApplicationOptions ceoApplicationOptions) {
		this.ceoApplicationOptions = ceoApplicationOptions;
	}
	
	public SurveyAvailability getAvailability() {
		return availability;
	}
}