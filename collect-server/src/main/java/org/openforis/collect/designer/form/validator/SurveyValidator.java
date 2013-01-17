package org.openforis.collect.designer.form.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyValidator {

	public List<SurveyValidationResult> validateSurvey(CollectSurvey survey) {
		return validateEnties(survey);
	}

	protected List<SurveyValidationResult> validateEnties(CollectSurvey survey) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidator.SurveyValidationResult>();
		Schema schema = survey.getSchema();
		Stack<EntityDefinition> entitiesStack = new Stack<EntityDefinition>();
		List<EntityDefinition> rootEntities = schema.getRootEntityDefinitions();
		entitiesStack.addAll(rootEntities);
		while ( ! entitiesStack.isEmpty() ) {
			EntityDefinition entity = entitiesStack.pop();
			List<NodeDefinition> childDefinitions = entity.getChildDefinitions();
			if ( childDefinitions.size() == 0 ) {
				String message = Labels.getLabel("survey.validation.empty_entity");
				String path = entity.getPath();
				SurveyValidationResult validationResult = new SurveyValidationResult(path, message);
				results.add(validationResult);
			} else {
				for (NodeDefinition childDefn : childDefinitions) {
					if ( childDefn instanceof EntityDefinition ) {
						entitiesStack.push((EntityDefinition) childDefn);
					}
				}
			}
		}
		return results;
	}
	
	public static class SurveyValidationResult {
		
		private String path;
		private String message;

		public SurveyValidationResult(String path, String message) {
			super();
			this.path = path;
			this.message = message;
		}

		public String getPath() {
			return path;
		}

		public String getMessage() {
			return message;
		}

	}
	
}
