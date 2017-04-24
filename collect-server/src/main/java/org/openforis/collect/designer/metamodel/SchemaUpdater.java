package org.openforis.collect.designer.metamodel;

import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.Layout;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel;

public class SchemaUpdater {

	private CollectSurvey survey;

	public SchemaUpdater(CollectSurvey survey) {
		super();
		this.survey = survey;
	}

	public void addChildDefinition(EntityDefinition parentDef, NodeDefinition childDef) {
		parentDef.addChildDefinition(childDef);
		//TODO add attribute clone into depending virtual entities
//		if (childDef instanceof AttributeDefinition) {
//			Set<EntityDefinition> dependingVirtualEntities = parentDef.calculateDependingVirtualEntities();
//			for (EntityDefinition virtualEntityDef : dependingVirtualEntities) {
//				virtualEntityDef.addChildDefinition(survey.getSchema().cloneDefinition(childDef));
//			}
//		}
	}

	public EntityDefinition generateAlias(EntityDefinition sourceDef, String sourceFilterAttributeName,  
			EntityDefinition targetParentDef, String targetFilterAttributeName) {
		EntityDefinition aliasDef = survey.getSchema().cloneDefinition(sourceDef, targetFilterAttributeName);
		//add "Alias" suffix to labels
		for (NodeLabel nodeLabel : aliasDef.getLabels()) {
			aliasDef.setLabel(nodeLabel.getType(), nodeLabel.getLanguage(), nodeLabel.getText() + " Alias");
		}
		targetParentDef.addChildDefinition(aliasDef);
		aliasDef.setVirtual(true);
		aliasDef.setGeneratorExpression(
				generateAliasGeneratorExpression(sourceDef, sourceFilterAttributeName, 
						targetParentDef, targetFilterAttributeName));
		UIOptions uiOptions = survey.getUIOptions();
		uiOptions.setLayout(aliasDef, Layout.FORM); //prevent layout errors
		uiOptions.setHidden(aliasDef, true);
		return aliasDef;
	}

	private String generateAliasGeneratorExpression(EntityDefinition sourceDef, String sourceFilterAttributeName,
			EntityDefinition targetParentDef, String targetFilterAttributeName) {
		return targetParentDef.getRelativePath(sourceDef) + 
				String.format("[%s=$context/%s]", sourceFilterAttributeName, targetFilterAttributeName);
	}
}
