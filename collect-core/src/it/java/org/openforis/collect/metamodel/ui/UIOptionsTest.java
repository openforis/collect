package org.openforis.collect.metamodel.ui;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.xml.IdmlParseException;

public class UIOptionsTest extends CollectIntegrationTest {
	
	@Test
	public void test() throws IdmlParseException, IOException {
		CollectSurvey survey = loadSurvey();
//		Schema schema = survey.getSchema();
//		List<EntityDefinition> rootEntityDefinitions = schema.getRootEntityDefinitions();
//		EntityDefinition cluster = rootEntityDefinitions.get(0);
//		EntityDefinition plot = (EntityDefinition) cluster.getChildDefinition("plot");
//		UIOptions uiOptions = survey.getUIOptions();
//		UITabSet rootTabSet = uiOptions.getAssignedRootTabSet(cluster);
//		Assert.assertNotNull(rootTabSet);
//		Assert.assertEquals("cluster", rootTabSet.getName());
//		List<UITab> plotAssignableTabs = uiOptions.getAssignableTabs(plot);
		
	}
	

}
