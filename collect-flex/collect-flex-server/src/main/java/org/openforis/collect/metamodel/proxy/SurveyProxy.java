package org.openforis.collect.metamodel.proxy;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.UIConfiguration;
import org.openforis.idm.metamodel.Configuration;
import org.openforis.idm.metamodel.Survey;

public class SurveyProxy implements Proxy {

	private static Log LOG = LogFactory.getLog(SurveyProxy.class);

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

	@ExternalizedProperty
	public List<CodeListProxy> getCodeLists() {
		return CodeListProxy.fromList(survey.getCodeLists());
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
	public UIConfiguration getUiConfiguration() {
		List<Configuration> configuration = survey.getConfiguration();
		if (configuration != null && !configuration.isEmpty()) {
			UIConfiguration uiConf = (UIConfiguration) configuration.get(0);
			return uiConf;
		} else {
			return null;
		}
	}

}
