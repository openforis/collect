package org.openforis.collect.metamodel.proxy;

import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.Unit;

public class SurveyProxy implements ProxyBase {

	private transient Survey survey;

	public SurveyProxy(Survey survey) {
		this.survey = survey;
	}

	@ExternalizedProperty
	public Integer getId() {
		return survey.getId();
	}

	public void setId(Integer id) {
		survey.setId(id);
	}

	@ExternalizedProperty
	public String getName() {
		return survey.getName();
	}

	@ExternalizedProperty
	public List<LanguageSpecificTextProxy> getProjectNames() {
		return LanguageSpecificTextProxy.fromList(survey.getProjectNames());
	}

	@ExternalizedProperty
	public Integer getCycle() {
		return survey.getCycle();
	}

	@ExternalizedProperty
	public List<LanguageSpecificTextProxy> getDescriptions() {
		return LanguageSpecificTextProxy.fromList(survey.getDescriptions());
	}

	@ExternalizedProperty
	public List<ModelVersionProxy> getVersions() {
		return ModelVersionProxy.fromList(survey.getVersions());
	}

	public List<CodeList> getCodeLists() {
		return survey.getCodeLists();
	}

	public List<Unit> getUnits() {
		return survey.getUnits();
	}

	public List<SpatialReferenceSystem> getSpatialReferenceSystems() {
		return survey.getSpatialReferenceSystems();
	}

	public Schema getSchema() {
		return survey.getSchema();
	}

}
