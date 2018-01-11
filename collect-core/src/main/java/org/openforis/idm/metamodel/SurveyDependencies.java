/**
 * 
 */
package org.openforis.idm.metamodel;

import java.util.List;
import java.util.Set;

import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.metamodel.validation.ComparisonCheck;
import org.openforis.idm.metamodel.validation.CustomCheck;
import org.openforis.idm.metamodel.validation.DistanceCheck;
import org.openforis.idm.metamodel.validation.UniquenessCheck;
import org.openforis.idm.model.NodePathPointer;
import org.openforis.idm.model.expression.ExpressionEvaluator;

/*
Ax: = ancestors of x
Dx := descendants of x
Lx := relevanceDependencies U relevanceDependencies descendants 
Qx := requiredDependencies of x
Rx := Lx U Qx
Vx := x U checkDependencies



On update value of attribute x, field.setXXX clear state of dependent nodes:
1. Clear relevance states of all nodes in Lx
2. Clear required states of all nodes in Rx
3. [Clear minCount validation state of all nodes in Rx U Ax] <-- skip for now
4. Clear all value validation results of nodes Vx

Send client updated states:
1. For each node in Rx U Ax: (entityId, childName, relevant, required, minCountValidation)*
2. For each node in Vx: (nodeId, validationResults)
3. Client updates UI accordingly 
  
* could be optimized further in the future by only sending updated relevance for Rx/Qx and relevance, required and missing for Rx/Lx
 
 */

/**
 * @author M. Togna
 * 
 */
class SurveyDependencies {

	private Survey survey;

	private StateDependencyMap calculatedValueDependencies;
	private StateDependencyMap minCountDependencies;
	private StateDependencyMap maxCountDependencies;
	private StateDependencyMap relevanceDependencies;
	private StateDependencyMap validationDependencies;
	private StateDependencyMap parentCodeDependencies;

	SurveyDependencies(Survey survey) {
		this.survey = survey;
		SurveyContext surveyContext = this.survey.getContext();
		ExpressionEvaluator expressionEvaluator = surveyContext.getExpressionEvaluator();
		
		this.calculatedValueDependencies = new StateDependencyMap(expressionEvaluator);
		this.minCountDependencies = new StateDependencyMap(expressionEvaluator);
		this.maxCountDependencies = new StateDependencyMap(expressionEvaluator);
		this.relevanceDependencies = new StateDependencyMap(expressionEvaluator);
		this.validationDependencies = new StateDependencyMap(expressionEvaluator);
		this.parentCodeDependencies = new StateDependencyMap(expressionEvaluator);
		
		registerDependencies();
	}

	private void registerDependencies() {
		Schema schema = survey.getSchema();
		
		//register dependencies for each root entity
		List<EntityDefinition> rootEntityDefns = schema.getRootEntityDefinitions();
		for (EntityDefinition rootDefn : rootEntityDefns) {
			registerDependencies(rootDefn);
			registerMultipleKeyDependencies(rootDefn);
		}
	}

	private void registerMultipleKeyDependencies(EntityDefinition entityDefn) {
		List<AttributeDefinition> keyDefns = entityDefn.getKeyAttributeDefinitions();
		if ( keyDefns.size() > 1 ) {
			for (AttributeDefinition keyDefn1 : keyDefns) {
				for (AttributeDefinition keyDefn2 : keyDefns) {
					if ( keyDefn2 != keyDefn1 ) {
						validationDependencies.registerDependencies(keyDefn1, keyDefn2);
					}
				}
			}
		}
	}

	private void registerDependencies(EntityDefinition entityDefinition) {
		List<NodeDefinition> childDefinitions = entityDefinition.getChildDefinitions();
		for (NodeDefinition nodeDefinition : childDefinitions) {
			relevanceDependencies.registerDependencies(nodeDefinition, nodeDefinition.getRelevantExpression());
			minCountDependencies.registerDependencies(nodeDefinition, nodeDefinition.getMinCountExpression());
			maxCountDependencies.registerDependencies(nodeDefinition, nodeDefinition.getMaxCountExpression());

			if (nodeDefinition instanceof AttributeDefinition) {
				registerDependencies((AttributeDefinition) nodeDefinition);
			} else {
				registerDependencies((EntityDefinition) nodeDefinition);
			}
		}
		if ( entityDefinition.isMultiple() ) {
			registerEntityKeyDependencies(entityDefinition);
		}
	}

	private void registerDependencies(AttributeDefinition defn) {
		//checks
		List<Check<?>> checks = defn.getChecks();
		for (Check<?> check : checks) {
			registerCheckDependencies(defn, check);
		}
		if(defn instanceof CodeAttributeDefinition){
			// register parent code dependencies
			CodeAttributeDefinition currentCodeDefn = (CodeAttributeDefinition) defn;
			while ( currentCodeDefn != null ) {
				String parentCodeExpression = currentCodeDefn.getParentExpression();
				validationDependencies.registerDependencies(defn, parentCodeExpression);
				parentCodeDependencies.registerDependencies(defn, parentCodeExpression);
				currentCodeDefn = currentCodeDefn.getParentCodeAttributeDefinition();
			}
		}
		//calculated values
		if ( defn.isCalculated() ) {
			List<AttributeDefault> attributeDefaults = defn.getAttributeDefaults();
			for (AttributeDefault attributeDefault : attributeDefaults) {
				calculatedValueDependencies.registerDependencies(defn, attributeDefault.getCondition());
				calculatedValueDependencies.registerDependencies(defn, attributeDefault.getExpression());
			}
		}
	}

	private void registerCheckDependencies(AttributeDefinition defn, Check<?> check) {
		validationDependencies.registerDependencies(defn, check.getCondition());
		if (check instanceof ComparisonCheck) {
			validationDependencies.registerDependencies(defn, ((ComparisonCheck) check).getEqualsExpression());
			validationDependencies.registerDependencies(defn, ((ComparisonCheck) check).getLessThanExpression());
			validationDependencies.registerDependencies(defn, ((ComparisonCheck) check).getLessThanOrEqualsExpression());
			validationDependencies.registerDependencies(defn, ((ComparisonCheck) check).getGreaterThanExpression());
			validationDependencies.registerDependencies(defn, ((ComparisonCheck) check).getGreaterThanOrEqualsExpression());
		} else if (check instanceof CustomCheck) {
			validationDependencies.registerDependencies(defn, ((CustomCheck) check).getExpression());
		} else if (check instanceof DistanceCheck) {
			validationDependencies.registerDependencies(defn, ((DistanceCheck) check).getDestinationPointExpression());
			validationDependencies.registerDependencies(defn, ((DistanceCheck) check).getMaxDistanceExpression());
			validationDependencies.registerDependencies(defn, ((DistanceCheck) check).getMinDistanceExpression());
			validationDependencies.registerDependencies(defn, ((DistanceCheck) check).getSourcePointExpression());
		} else if (check instanceof UniquenessCheck) {
			validationDependencies.registerDependencies(defn, ((UniquenessCheck) check).getExpression());
		}
	}

	protected void registerEntityKeyDependencies(EntityDefinition defn) {
		if ( defn.getParentDefinition() == null ) {
			//skip entity key validation for root entities
			return;
		}
		EntityDefinition ancestorMultipleEntity = defn.getNearestAncestorMultipleEntity();
		List<AttributeDefinition> keyAttributeDefinitions = defn.getKeyAttributeDefinitions();
		for (AttributeDefinition keyDefn : keyAttributeDefinitions) {
			for (AttributeDefinition otherKeyDefn : keyAttributeDefinitions) {
				validationDependencies.registerDependent(ancestorMultipleEntity, keyDefn, otherKeyDefn);
				validationDependencies.registerSource(ancestorMultipleEntity, keyDefn, otherKeyDefn);
			}
		}
	}

	Set<NodePathPointer> getCalculatedValueDependencies(NodeDefinition definition) {
		return calculatedValueDependencies.getDependents(definition);
	}
	
	Set<NodePathPointer> getCalculatedValueSources(NodeDefinition definition) {
		return calculatedValueDependencies.getSources(definition);
	}
	
	Set<NodePathPointer> getMinCountDependencies(NodeDefinition definition) {
		return minCountDependencies.getDependents(definition);
	}
	
	Set<NodePathPointer> getMinCountSources(NodeDefinition definition) {
		return minCountDependencies.getSources(definition);
	}
	
	Set<NodePathPointer> getMaxCountDependencies(NodeDefinition definition) {
		return maxCountDependencies.getDependents(definition);
	}
	
	Set<NodePathPointer> getMaxCountSources(NodeDefinition definition) {
		return maxCountDependencies.getSources(definition);
	}
	
	Set<NodePathPointer> getRelevanceDependencies(NodeDefinition definition) {
		return relevanceDependencies.getDependents(definition);
	}
	
	Set<NodePathPointer> getRelevanceSources(NodeDefinition definition) {
		return relevanceDependencies.getSources(definition);
	}
	
	Set<NodeDefinition> getRelevanceSourceNodeDefinitions(NodeDefinition def) {
		return relevanceDependencies.getSourceNodeDefinitions(def);
	}

	Set<NodePathPointer> getValidationDependencies(NodeDefinition definition) {
		return validationDependencies.getDependents(definition);
	}
	
	Set<NodePathPointer> getValidationSources(NodeDefinition definition) {
		return validationDependencies.getSources(definition);
	}
	
	Set<NodePathPointer> getRelatedCodeDependencies(CodeAttributeDefinition definition) {
		return parentCodeDependencies.getDependents(definition);
	}
	
	Set<NodePathPointer> getRelatedCodeSources(CodeAttributeDefinition definition) {
		return parentCodeDependencies.getSources(definition);
	}

}
