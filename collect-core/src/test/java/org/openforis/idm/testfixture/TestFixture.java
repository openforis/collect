package org.openforis.idm.testfixture;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.TestSurveyContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestFixture {
	public final Survey survey;
	public final List<Record> records;

	public TestFixture(Survey survey, List<Record> records) {
		this.survey = survey;
		this.records = records;
	}

	public static TestFixture survey(NodeDefinitionBuilder.EntityDefinitionBuilder entityDefinitionBuilder,
	                                 RecordBuilder... recordBuilders) {
		SurveyContext surveyContext = new TestSurveyContext();
		Survey survey = surveyContext.createSurvey();
		EntityDefinition rootEntityDef = (EntityDefinition) entityDefinitionBuilder.buildInternal(survey, null);
		survey.getSchema().addRootEntityDefinition(rootEntityDef);
		survey.refreshSurveyDependencies();

		List<Record> records = new ArrayList<Record>();
		for (RecordBuilder recordBuilder : recordBuilders) {
			Record record = recordBuilder.build(survey);
			records.add(record);
		}
		return new TestFixture(survey, Collections.unmodifiableList(records));
	}
}
