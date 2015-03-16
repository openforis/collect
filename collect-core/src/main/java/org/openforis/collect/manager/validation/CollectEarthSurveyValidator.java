package org.openforis.collect.manager.validation;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResult;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResults;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class CollectEarthSurveyValidator {

	//TODO use CollectEarthBalloonGenerator.HIDDEN_ATTRIBUTE_NAMES 
	private static final String[] REQUIRED_FIELDS = new String[] {
		"id", "operator", "location", "plot_file", "actively_saved", "actively_saved_on", "elevation", "slope", "aspect"
	};
	
	public SurveyValidationResults validate(CollectSurvey survey) {
		final SurveyValidationResults results = new SurveyValidationResults();
		
		//check missing required fields
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootEntityDefinitions = schema.getRootEntityDefinitions();
		
		EntityDefinition rootEntity = rootEntityDefinitions.get(0);
		
		//check all required fields defined
		for (String field: REQUIRED_FIELDS) {
			try {
				rootEntity.getChildDefinition(field);
			} catch(Exception e) {
				results.addResult(new SurveyValidationResult(rootEntity.getPath() + "/" + field, "survey.validation.collect_earth.missing_required_field"));
			}
		}
		
		//check valid node definitions (not nested multiple entities, only enumerable entities)ww
		List<NodeDefinition> nextLevelDefs = new ArrayList<NodeDefinition>();
		nextLevelDefs.addAll(rootEntity.getChildDefinitions());
		
		for (int currentLevelIndex = 0; currentLevelIndex < 2; currentLevelIndex ++) {
			List<NodeDefinition> currentLevelDefs = nextLevelDefs;
			nextLevelDefs = new ArrayList<NodeDefinition>();
			for (NodeDefinition nodeDef : currentLevelDefs) {
				if (nodeDef instanceof EntityDefinition) {
					EntityDefinition entityDef = (EntityDefinition) nodeDef;
					if (currentLevelIndex == 0) {
						if (nodeDef.isMultiple() && ! entityDef.isEnumerable()) {
							results.addResult(new SurveyValidationResult(entityDef.getPath(), "survey.validation.collect_earth.multiple_entities_not_supported"));
						} else {
							nextLevelDefs.addAll(entityDef.getChildDefinitions());
						}
					} else {
						results.addResult(new SurveyValidationResult(nodeDef.getPath(), "survey.validation.collect_earth.nested_entities_not_supported"));
					}
				}
			}
		}
		return results;
	}

}
