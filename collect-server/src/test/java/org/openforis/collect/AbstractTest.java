package org.openforis.collect;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.Before;
import org.junit.BeforeClass;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTestSurveyContext;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.metamodel.xml.SurveyIdmlBinder;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Record;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class AbstractTest {

	protected static Survey survey;
	protected Entity cluster;
	protected Entity household;
	protected Record record;

	@BeforeClass
	public static void setUp() throws IOException, XmlParseException, IdmlParseException {
		URL idm = AbstractTest.class.getResource("/test.idm.xml");
		InputStream is = idm.openStream();
		SurveyIdmlBinder binder = new SurveyIdmlBinder(new CollectTestSurveyContext());
		survey = (CollectSurvey) binder.unmarshal(is);
	}

	@Before
	public void createCluster() {
		this.record = new Record(survey, "2.0", "cluster");
		this.cluster = record.getRootEntity();
		Record record2 = new Record(survey, "2.0", "household");
		this.household = record2.getRootEntity();
	}
}