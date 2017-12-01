package org.openforis.idm.model;

import org.junit.Test;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.testfixture.TestFixture;

import static org.junit.Assert.assertSame;
import static org.openforis.idm.testfixture.NodeDefinitionBuilder.rootEntityDef;
import static org.openforis.idm.testfixture.TestFixture.survey;

public class RecordTest {

	@Test
	public void testRootEntityCanBeReplaced() {
		TestFixture fixture = survey(
				rootEntityDef("plot")
		);

		Record record = new Record(fixture.survey, null, "plot");
		EntityDefinition rootEntityDef = fixture.survey.getSchema().getRootEntityDefinition("plot");
		Entity rootEntity = new Entity(rootEntityDef);
		record.replaceRootEntity(rootEntity);
		assertSame(rootEntity, record.getRootEntity());
	}


}
