package org.openforis.idm.testfixture;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.TestSurveyContext;
import org.openforis.idm.testfixture.NodeDefinitionBuilder.AttributeDefinitionBuilder;
import org.openforis.idm.testfixture.NodeDefinitionBuilder.EntityDefinitionBuilder;

public class SurveyBuilder {
	
	private CollectSurvey survey = new TestSurveyContext().createSurvey();
	private NodeDefinitionBuilder[] rootEntityNodeBuilders;
	private CodeListBuilder[] codeListBuilders;

	public static Survey survey(NodeDefinitionBuilder... builders) {
		String rootEntityName = "root";
		Survey survey = new TestSurveyContext().createSurvey();
		EntityDefinitionBuilder entityBuilder = new EntityDefinitionBuilder(rootEntityName, builders);
		EntityDefinition rootEntityDef = (EntityDefinition) entityBuilder.buildInternal(survey, null);
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
	
	public static CodeListBuilder codeList(String name) {
		return new CodeListBuilder(name);
	}
	
	public SurveyBuilder rootEntityDef(NodeDefinitionBuilder... nodeBuilders) {
		this.rootEntityNodeBuilders = nodeBuilders;
		return this;
	}
	
	public SurveyBuilder codeLists(CodeListBuilder... codeListBuilders) {
		this.codeListBuilders = codeListBuilders;
		return this;
	}
	
	public CollectSurvey build() {
		for (CodeListBuilder codeListBuilder : codeListBuilders) {
			CodeList list = codeListBuilder.build(survey);
			survey.addCodeList(list);
		}
		
		for (NodeDefinitionBuilder rootEntityBuilder : rootEntityNodeBuilders) {
			EntityDefinition rootEntity = (EntityDefinition) rootEntityBuilder.buildInternal(survey, null);
			survey.getSchema().addRootEntityDefinition(rootEntity);
		}
		return survey;
	}
	
}