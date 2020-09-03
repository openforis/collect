package org.openforis.collect.metamodel.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.metamodel.ui.UIConfiguration;
import org.openforis.collect.metamodel.uiconfiguration.view.UIConfigurationView;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveyAvailability;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserInGroup.UserGroupRole;
import org.openforis.collect.persistence.xml.CeoApplicationOptions;

public class SurveyView {
	
	private Integer id;
	private String name;
	private String projectName;
	private String description;
	private List<String> languages;
	private String defaultLanguage;
	private boolean temporary;
	private SurveyTarget target;
	private int userGroupId;
	private UserGroup userGroup;
	private UserGroupRole userInGroupRole;
	private String userGroupQualifierName;
	private String userGroupQualifierValue;
	private SchemaView schema;
	private List<CodeListView> codeLists = new ArrayList<CodeListView>();
	private List<ModelVersionView> modelVersions = new ArrayList<ModelVersionView>();
	private CeoApplicationOptions ceoApplicationOptions;
	private UIConfiguration uiConfiguration;
	private SurveyAvailability availability;
	private Date creationDate;
	private Date modifiedDate;
	
	private ViewContext context;
	
	public SurveyView(Integer id, String name, boolean temporary, SurveyTarget target, ViewContext context) {
		this.id = id;
		this.name = name;
		this.temporary = temporary;
		this.target = target;
		this.context = context;
	}
	
	public SurveyView(CollectSurvey s) {
		this(s, new ViewContext(s.getDefaultLanguage()));
	}
	
	public SurveyView(CollectSurvey s, ViewContext context) {
		this(s.getId(), s.getName(), s.isTemporary(), s.getTarget(), context);
		this.projectName = s.getProjectName();
		this.description = s.getDescription();
		this.defaultLanguage = s.getDefaultLanguage();
		this.languages = s.getLanguages();
		this.userGroupId = s.getUserGroupId();
		this.userGroup = s.getUserGroup();
		this.schema = new SchemaView();
		this.uiConfiguration = s.getUIConfiguration();
		this.ceoApplicationOptions = s.getApplicationOptions(CeoApplicationOptions.TYPE);
		this.availability = s.getAvailability();
		this.modifiedDate = s.getModifiedDate();
		this.creationDate = s.getCreationDate();
	}

	public UIConfigurationView getUiConfiguration() {
		return new UIConfigurationView(uiConfiguration, context);
	}
	
	public void addCodeList(CodeListView codeListView) {
		this.codeLists.add(codeListView);
	}
	
	public void addModelVersion(ModelVersionView modelVersion) {
		this.modelVersions.add(modelVersion);
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
	
	public List<String> getLanguages() {
		return languages;
	}
	
	public void setLanguages(List<String> languages) {
		this.languages = languages;
	}
	
	public String getDefaultLanguage() {
		return defaultLanguage;
	}
	
	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
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
	
	public int getUserGroupId() {
		return userGroupId;
	}
	
	public void setUserGroupId(int userGroupId) {
		this.userGroupId = userGroupId;
	}
	
	public UserGroup getUserGroup() {
		return userGroup;
	}
	
	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = userGroup;
	}
	
	public String getUserGroupQualifierName() {
		return userGroupQualifierName;
	}
	
	public void setUserGroupQualifierName(String userGroupQualifierName) {
		this.userGroupQualifierName = userGroupQualifierName;
	}
	
	public String getUserGroupQualifierValue() {
		return userGroupQualifierValue;
	}
	
	public void setUserGroupQualifierValue(String userGroupQualifierValue) {
		this.userGroupQualifierValue = userGroupQualifierValue;
	}
	
	public UserGroupRole getUserInGroupRole() {
		return userInGroupRole;
	}
	
	public void setUserInGroupRole(UserGroupRole userInGroupRole) {
		this.userInGroupRole = userInGroupRole;
	}
	
	public List<CodeListView> getCodeLists() {
		return codeLists;
	}
	
	public List<ModelVersionView> getModelVersions() {
		return modelVersions;
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
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public Date getModifiedDate() {
		return modifiedDate;
	}
}