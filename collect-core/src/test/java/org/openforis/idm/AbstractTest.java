package org.openforis.idm;

import java.io.InputStream;

import org.junit.Before;
import org.junit.BeforeClass;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.metamodel.xml.SurveyIdmlBinder;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.TestSurveyContext;
import org.openforis.idm.model.expression.ExpressionEvaluator;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class AbstractTest {

	protected static Survey survey;
	protected static ExpressionEvaluator expressionEvaluator;
	protected Entity cluster;
	protected Entity household;
	protected Record record;

	@BeforeClass
	public static void setUp() throws Exception {
		survey = createTestSurvey();
		expressionEvaluator = survey.getContext().getExpressionEvaluator();
	}

	protected static Survey createTestSurvey() throws IdmlParseException {
		InputStream is = AbstractTest.class.getClassLoader().getResourceAsStream("test.idm.xml");
		SurveyContext surveyContext = new TestSurveyContext();
		SurveyIdmlBinder parser = new SurveyIdmlBinder(surveyContext);
		Survey survey = parser.unmarshal(is);
		return survey;
	}

	@Before
	public void createCluster() {
		this.record = new Record(survey, "2.0", "cluster");
		this.cluster = record.getRootEntity();
		Record record2 = new Record(survey, "2.0", "household");
		this.household = record2.getRootEntity();
	}
}
