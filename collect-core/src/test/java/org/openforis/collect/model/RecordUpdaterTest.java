/**
 * 
 */
package org.openforis.collect.model;

import static org.junit.Assert.*;
import static org.openforis.idm.metamodel.NodeDefinitionBuilder.*;
import static org.openforis.collect.model.NodeBuilder.*;

import org.junit.Before;
import org.junit.Test;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.TestSurveyContext;
import org.openforis.idm.model.TextValue;
import org.openforis.idm.model.Value;

/**
 * @author D. Wiell
 * @author S. Ricci
 *
 */
@SuppressWarnings("unchecked")
public class RecordUpdaterTest {

	private RecordUpdater updater;
	private Survey survey;
	private Record record;

	@Before
	public void init() {
		survey = createTestSurvey();
		updater = new RecordUpdater();
	}
	
	@Test
	public void testUpdateAttribute() {
		rootEntityDef(survey, "root", 
				attributeDef("attribute"));
		
		record = record(survey, 
				attribute("attribute", "initial value")
		);
		
		Attribute<?,?> attr = findAttribute("root/attribute[1]");
		
		NodeChangeSet result = update(attr, "new value");
		
		assertNotNull(result);
		assertTrue(result.size() == 1);
		
		AttributeChange attrChange = (AttributeChange) result.getChange(attr);
		
		assertNotNull(attrChange);
	}

	protected NodeChangeSet updateAttribute(String path, String value) {
		Attribute<?,?> attr = findAttribute(path);
		NodeChangeSet result = update(attr, value);
		return result;
	}

	protected NodeChangeSet update(Attribute<?, ?> attr, String value) {
		return updater.updateAttribute((Attribute<?, Value>) attr, new TextValue(value));
	}

	protected Attribute<?,?>  findAttribute(String path) {
		return (Attribute<?, ?>) record.findNodeByPath(path);
	}
	
	private Survey createTestSurvey() {
		SurveyContext surveyContext = new TestSurveyContext();
		Survey survey = surveyContext.createSurvey();
		return survey;
	}
	
}
