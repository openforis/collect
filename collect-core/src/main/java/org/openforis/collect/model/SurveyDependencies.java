/**
 * 
 */
package org.openforis.collect.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.metamodel.validation.ComparisonCheck;
import org.openforis.idm.metamodel.validation.CustomCheck;
import org.openforis.idm.metamodel.validation.DistanceCheck;
import org.openforis.idm.metamodel.validation.UniquenessCheck;
import org.openforis.idm.model.expression.ExpressionFactory;

/**
 * @author M. Togna
 * 
 */
public class SurveyDependencies {

	private ExpressionFactory expressionFactory;

	private Survey survey;
	private StateDependencyMap relevantDependencies;
	private StateDependencyMap requiredDependencies;
	private StateDependencyMap defaultValueDependencies;
	private StateDependencyMap checkDependencies;

	public SurveyDependencies(ExpressionFactory expressionFactory) {
		this.expressionFactory = expressionFactory;
		reset();
	}

	public Set<String> getDependantPaths(NodeDefinition definition) {
		Set<String> set = new HashSet<String>();
		String path = definition.getPath();
		set.addAll(relevantDependencies.getDependantPaths(path));
		set.addAll(requiredDependencies.getDependantPaths(path));
		set.addAll(defaultValueDependencies.getDependantPaths(path));
		set.addAll(checkDependencies.getDependantPaths(path));
		return set;
	}

	public void register(Survey survey) {
		this.survey = survey;
		reset();

		Schema schema = survey.getSchema();
		List<EntityDefinition> rootEntityDefinitions = schema.getRootEntityDefinitions();
		for (EntityDefinition entityDefinition : rootEntityDefinitions) {
			register(entityDefinition);
		}
	}

	private void register(EntityDefinition entityDefinition) {
		List<NodeDefinition> childDefinitions = entityDefinition.getChildDefinitions();
		for (NodeDefinition nodeDefinition : childDefinitions) {
			relevantDependencies.register(nodeDefinition, nodeDefinition.getRelevantExpression());
			requiredDependencies.register(nodeDefinition, nodeDefinition.getRequiredExpression());

			if (nodeDefinition instanceof AttributeDefinition) {
				register((AttributeDefinition) nodeDefinition);
			} else {
				register((EntityDefinition) nodeDefinition);
			}
		}
	}

	private void register(AttributeDefinition attributeDefinition) {
		List<AttributeDefault> attributeDefaults = attributeDefinition.getAttributeDefaults();
		for (AttributeDefault attributeDefault : attributeDefaults) {
			defaultValueDependencies.register(attributeDefinition, attributeDefault.getCondition());
			defaultValueDependencies.register(attributeDefinition, attributeDefault.getExpression());
		}
		List<Check> checks = attributeDefinition.getChecks();
		for (Check check : checks) {
			checkDependencies.register(attributeDefinition, check.getCondition());
			if (check instanceof ComparisonCheck) {
				checkDependencies.register(attributeDefinition, ((ComparisonCheck) check).getEqualsExpression());
				checkDependencies.register(attributeDefinition, ((ComparisonCheck) check).getLessThanExpression());
				checkDependencies.register(attributeDefinition, ((ComparisonCheck) check).getLessThanOrEqualsExpression());
				checkDependencies.register(attributeDefinition, ((ComparisonCheck) check).getGreaterThanExpression());
				checkDependencies.register(attributeDefinition, ((ComparisonCheck) check).getGreaterThanOrEqualsExpression());
			} else if (check instanceof CustomCheck) {
				checkDependencies.register(attributeDefinition, ((CustomCheck) check).getExpression());
			} else if (check instanceof DistanceCheck) {
				checkDependencies.register(attributeDefinition, ((DistanceCheck) check).getDestinationPointExpression());
				checkDependencies.register(attributeDefinition, ((DistanceCheck) check).getMaxDistanceExpression());
				checkDependencies.register(attributeDefinition, ((DistanceCheck) check).getMinDistanceExpression());
				checkDependencies.register(attributeDefinition, ((DistanceCheck) check).getSourcePointExpression());
			} else if (check instanceof UniquenessCheck) {
				checkDependencies.register(attributeDefinition, ((UniquenessCheck) check).getExpression());
			}
		}
	}

	private void reset() {
		relevantDependencies = new StateDependencyMap(expressionFactory);
		requiredDependencies = new StateDependencyMap(expressionFactory);
		defaultValueDependencies = new StateDependencyMap(expressionFactory);
		checkDependencies = new StateDependencyMap(expressionFactory);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		String name = survey.getName();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SurveyDependencies other = (SurveyDependencies) obj;
		if (survey.getName() == null) {
			if (other.survey.getName() != null)
				return false;
		} else if (!survey.getName().equals(other.survey.getName()))
			return false;
		return true;
	}

}
