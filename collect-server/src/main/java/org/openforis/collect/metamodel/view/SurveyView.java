package org.openforis.collect.metamodel.view;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.metamodel.ui.UIConfiguration;
import org.openforis.collect.metamodel.uiconfiguration.view.UIConfigurationView;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.Institution;
import org.openforis.collect.persistence.xml.CeoApplicationOptions;

public class SurveyView {
	
	private Integer id;
	private String name;
	private boolean temporary;
	private SurveyTarget target;
	private Institution institution;
	private SchemaView schema;
	private List<CodeListView> codeLists = new ArrayList<CodeListView>();
	private CeoApplicationOptions ceoApplicationOptions;
	private UIConfiguration uiConfiguration;
	
	public SurveyView(Integer id, String name, boolean temporary, SurveyTarget target) {
		this.id = id;
		this.name = name;
		this.temporary = temporary;
		this.target = target;
	}
	
	public SurveyView(CollectSurvey survey) {
		this(survey.getId(), survey.getName(), survey.isTemporary(), survey.getTarget());
		this.institution = survey.getInstitution();
		this.schema = new SchemaView();
		this.uiConfiguration = survey.getUIConfiguration();
		this.ceoApplicationOptions = survey.getApplicationOptions(CeoApplicationOptions.TYPE);
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
	
	public Institution getInstitution() {
		return institution;
	}
	
	public void setInstitution(Institution institution) {
		this.institution = institution;
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

}