package org.openforis.collect.mondrian;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;

/**
 * 
 * @author S. Ricci
 *
 */
public class MondrianSchemaGenerator {

	private CollectSurvey survey;
	private RelationalSchemaConfig rdbConfig;
	private String language;
	private String dbSchemaName;
	
	public MondrianSchemaGenerator(CollectSurvey survey, String language, String dbSchemaName, RelationalSchemaConfig rdbConfig) {
		this.survey = survey;
		this.language = language;
		this.rdbConfig = rdbConfig;
		this.dbSchemaName = dbSchemaName;
	}
	
	public void generate() {
		RelationalSchemaGenerator rdbGenerator = new RelationalSchemaGenerator(rdbConfig);
		RelationalSchema rdbSchema = rdbGenerator.generateSchema(survey, dbSchemaName);
		rdbSchema.getDataTables();
	}
}
