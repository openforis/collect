package org.openforis.collect.designer.form.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.openforis.collect.manager.SurveyManager;
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

	@SuppressWarnings("unused")
	private SurveyManager surveyManager;

	public SurveyValidator(SurveyManager surveyManager) {
		this.surveyManager = surveyManager;
	}
	
	public List<SurveyValidationResult> validateSurvey(CollectSurvey survey) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidator.SurveyValidationResult>();
		List<SurveyValidationResult> partialResults = validateEnties(survey);
		results.addAll(partialResults);
//		partialResults = validateExpressions(survey);
//		results.addAll(partialResults);
		return results;
	}
	
	/**
	 * Checks for the existence of empty entities
	 * 
	 * @param survey
	 * @return
	 */
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
				String message = Labels.getLabel("survey.validation.error.empty_entity");
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
	
	/*
	protected List<SurveyValidationResult> validateExpressions(CollectSurvey survey) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidator.SurveyValidationResult>();
		Schema schema = survey.getSchema();
		Stack<NodeDefinition> nodesStack = new Stack<NodeDefinition>();
		List<EntityDefinition> rootEntities = schema.getRootEntityDefinitions();
		nodesStack.addAll(rootEntities);
		while ( ! nodesStack.isEmpty() ) {
			NodeDefinition node = nodesStack.pop();
			List<SurveyValidationResult> nodeValidationResults = validateExpressions(node);
			if ( ! nodeValidationResults.isEmpty() ) {
				results.addAll(nodeValidationResults);
			}
			if ( node instanceof EntityDefinition ) {
				List<NodeDefinition> childDefns = ((EntityDefinition) node).getChildDefinitions();
				if ( ! childDefns.isEmpty() ) {
					nodesStack.addAll(childDefns);
				}
			}
		}
		return results;
	}
	
	private List<SurveyValidationResult> validateExpressions(NodeDefinition node) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidator.SurveyValidationResult>();
		NodeDefinition parentDefn = node.getParentDefinition();
		String path = node.getPath();
		String expression = node.getRelevantExpression();
		if ( StringUtils.isNotBlank(expression) ) {
		}
		if ( node.getMinCount() > 0 ) {
			expression = node.getRequiredExpression();
			if ( StringUtils.isNotBlank(expression) ) {
			}
		}
		if ( node instanceof CodeAttributeDefinition ) {
			CodeAttributeDefinition codeDefn = (CodeAttributeDefinition) node;
			String expr = codeDefn.getParentExpression();
			if ( StringUtils.isNotBlank(expr) && ! surveyManager.validatePathExpression(parentDefn, expr) ) {
				String message = Labels.getLabel("survey.schema.attribute.code.validation.error.invalid_parent_expression");
				SurveyValidationResult surveyValidationResult = new SurveyValidationResult(path, message);
				results.add(surveyValidationResult);
			}
		} else if ( node instanceof TaxonAttributeDefinition ) {
			List<String> qualifiers = ((TaxonAttributeDefinition) node).getQualifiers();
			if ( qualifiers != null ) {
				for (String expr : qualifiers) {
					if ( StringUtils.isNotBlank(expr) && ! surveyManager.validatePathExpression(parentDefn, expr) ) {
						String message = Labels.getLabel("survey.schema.attribute.taxon.validation.error.invalid_qualifier_expression");
						SurveyValidationResult surveyValidationResult = new SurveyValidationResult(path, message);
						results.add(surveyValidationResult);
						break;
					}
				}
			}
		}
		return results;
	}
	*/
	
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
