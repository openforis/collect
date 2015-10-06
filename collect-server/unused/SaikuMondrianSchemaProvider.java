package org.openforis.collect.saiku;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mondrian.olap.MondrianDef.Schema;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.collect.relational.mondrian.Mondrian4SchemaGenerator;

/**
 * 
 * @author S. Ricci
 *
 */
public class SaikuMondrianSchemaProvider {
	
	private SurveyManager surveyManager;
	private Map<String, Schema> mondrianSchemaDefinitionBySurveyName;
	private RelationalSchemaConfig rdbConfig = RelationalSchemaConfig.createDefault();

	public SaikuMondrianSchemaProvider(SurveyManager surveyManager) {
		this.surveyManager = surveyManager;
		this.mondrianSchemaDefinitionBySurveyName = new HashMap<String, Schema>();
	}

	public void init() {
		initializeMondrianSchemaDefinitions();
	}

	public Schema getMondrianSchema(String surveyName) {
		return mondrianSchemaDefinitionBySurveyName.get(surveyName);
	}
	
	private void initializeMondrianSchemaDefinitions() {
		List<CollectSurvey> surveys = surveyManager.getAll();
		for (CollectSurvey survey : surveys) {
			initializeMondrianSchemaDefinition(survey);
		}
	}
	
	private void initializeMondrianSchemaDefinition(CollectSurvey survey) {
		Mondrian4SchemaGenerator generator = new Mondrian4SchemaGenerator(survey, rdbConfig);
		Schema schema = generator.generateSchema();
		mondrianSchemaDefinitionBySurveyName.put(survey.getName(), schema);
	}

}
