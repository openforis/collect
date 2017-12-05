package org.openforis.collect.model;

import org.junit.Before;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.TextValue;
import org.openforis.idm.model.Value;
import org.openforis.idm.testfixture.NodeBuilder;
import org.openforis.idm.testfixture.NodeDefinitionBuilder;

public abstract class AbstractRecordTest {
	protected Survey survey;
	protected RecordUpdater updater;
	protected Record record;

	@Before
	public void init() {
		survey = createTestSurvey();
		updater = new RecordUpdater();
	}
	
	protected CollectSurvey createTestSurvey() {
		CollectSurveyContext surveyContext = new CollectSurveyContext();
		CollectSurvey survey = (CollectSurvey) surveyContext.createSurvey();
		return survey;
	}

	protected void record(EntityDefinition rootDef, NodeBuilder... builders) {
		record = NodeBuilder.record(survey, builders);
		updater.initializeRecord(record);
	}

	protected void record(EntityDefinition rootDef, String versionName, NodeBuilder... builders) {
		record = NodeBuilder.record(survey, rootDef.getName(), versionName, builders);
		updater.initializeRecord(record);
	}

	protected EntityDefinition rootEntityDef(NodeDefinitionBuilder... builders) {
		EntityDefinition rootEntityDef = NodeDefinitionBuilder.rootEntityDef(survey, "root", builders);
		return rootEntityDef;
	}

	protected Entity entityByPath(String path) {
		return record.findNodeByPath(path);
	}

	protected NodeChangeSet updateAttribute(String path, String value) {
		Attribute<?,?> attr = attributeByPath(path);
		NodeChangeSet result = update(attr, value);
		return result;
	}

	protected NodeChangeSet update(String path, String value) {
		return update(attributeByPath(path), value);
	}

	@SuppressWarnings("unchecked")
	protected NodeChangeSet update(Attribute<?, ?> attr, String value) {
		return updater.updateAttribute((Attribute<?, Value>) attr, new TextValue(value));
	}

	protected Attribute<?,?> attributeByPath(String path) {
		return record.findNodeByPath(path);
	}

}