package org.openforis.collect.relational;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.collect.relational.mondrian.MondrianSchemaGenerator;
import org.openforis.collect.relational.mondrian.Schema;

public class SaikuReporting {
	
	private SurveyManager surveyManager;
	private Map<String, Schema> mondrianSchemaDefinitionBySurveyName;
	private RelationalSchemaConfig rdbConfig = RelationalSchemaConfig.createDefault();

	public SaikuReporting(SurveyManager surveyManager) {
		this.surveyManager = surveyManager;
		this.mondrianSchemaDefinitionBySurveyName = new HashMap<String, Schema>();
	}

	public void init() {
		initializeMondrianSchemaDefinitions();
	}

	private void initializeMondrianSchemaDefinitions() {
		List<CollectSurvey> surveys = surveyManager.getAll();
		for (CollectSurvey survey : surveys) {
			initializeMondrianSchemaDefinition(survey);
		}
	}
	
	private void initializeMondrianSchemaDefinition(CollectSurvey survey) {
		MondrianSchemaGenerator generator = new MondrianSchemaGenerator(survey, rdbConfig);
		Schema schema = generator.generateSchema();
		mondrianSchemaDefinitionBySurveyName.put(survey.getName(), schema);
	}

}
