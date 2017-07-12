package org.openforis.collect.metamodel.proxy;

import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.model.CollectSurvey;

public class SurveyProxy implements Proxy {

	private transient CollectSurvey survey;

	public SurveyProxy(CollectSurvey survey) {
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
	public String getUri() {
		return survey.getUri();
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
	public String getCycle() {
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

	@ExternalizedProperty
	public List<UnitProxy> getUnits() {
		return UnitProxy.fromList(survey.getUnits());
	}

	@ExternalizedProperty
	public List<SpatialReferenceSystemProxy> getSpatialReferenceSystems() {
		return SpatialReferenceSystemProxy.fromList(survey.getSpatialReferenceSystems());
	}

	@ExternalizedProperty
	public SchemaProxy getSchema() {
		return new SchemaProxy(survey.getSchema());
	}

	@ExternalizedProperty
	public UIOptionsProxy getUiOptions() {
		UIOptions uiOptions = survey.getUIOptions();
		return new UIOptionsProxy(uiOptions);
	}

	@ExternalizedProperty
	public List<String> getLanguages() {
		return survey.getLanguages();
	}
	
	@ExternalizedProperty
	public String getDefaultLanguageCode() {
		return survey.getLanguages().get(0);
	}
	
	@ExternalizedProperty
	public boolean isKeyChangeAllowed() {
		return survey.getAnnotations().isKeyChangeAllowed();
	}
	
}
