package org.openforis.idm.testfixture;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.TestSurveyContext;
import org.openforis.idm.testfixture.NodeDefinitionBuilder.AttributeDefinitionBuilder;
import org.openforis.idm.testfixture.NodeDefinitionBuilder.EntityDefinitionBuilder;

public class SurveyBuilder {
	
	private Survey survey = new TestSurveyContext().createSurvey();
	private NodeDefinitionBuilder[] rootEntityNodeBuilders;

	public static Survey survey(NodeDefinitionBuilder... builders) {
		String rootEntityName = "root";
		Survey survey = new TestSurveyContext().createSurvey();
		EntityDefinitionBuilder entityBuilder = new EntityDefinitionBuilder(rootEntityName, builders);
		EntityDefinition rootEntityDef = (EntityDefinition) entityBuilder.buildInternal(survey);
		Schema schema = survey.getSchema();
		if ( schema.getRootEntityDefinition(rootEntityName) != null ) {
			schema.removeRootEntityDefinition(rootEntityName);
		}
		schema.addRootEntityDefinition(rootEntityDef);
		survey.refreshSurveyDependencies();
		return survey;
	}
	
	public static NodeDefinitionBuilder entityDef(String name, NodeDefinitionBuilder... builders) {
		return new EntityDefinitionBuilder(name, builders);
	}
	
	public static AttributeDefinitionBuilder attributeDef(String name) {
		return new AttributeDefinitionBuilder(name);
	}
	
	public SurveyBuilder rootEntityDef(NodeDefinitionBuilder... nodeBuilders) {
		this.rootEntityNodeBuilders = nodeBuilders;
		return this;
	}
	
	public Survey build() {
		EntityDefinitionBuilder rootEntityBuilder = NodeDefinitionBuilder.rootEntityDef("root", rootEntityNodeBuilders);
		rootEntityBuilder.buildInternal(survey);
		return survey;
	}
	
}