/**
 * 
 */
package org.openforis.collect.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.expression.SchemaPathExpression;
import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.metamodel.validation.ComparisonCheck;
import org.openforis.idm.metamodel.validation.CustomCheck;
import org.openforis.idm.metamodel.validation.DistanceCheck;
import org.openforis.idm.metamodel.validation.UniquenessCheck;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.InvalidExpressionException;
import org.openforis.idm.model.expression.ModelPathExpression;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 * 
 */
public class StateDependencyMap {

	private static final Log LOG = LogFactory.getLog(StateDependencyMap.class);

	private Map<Integer, Map<String, String>> relevantDependencies;
	private Map<Integer, Map<String, String>> requiredDependencies;
	private Map<Integer, Map<String, String>> defaultValueDependencies;
	private Map<Integer, Map<String, String>> checkDependencies;

	public StateDependencyMap() {
		relevantDependencies = new HashMap<Integer, Map<String, String>>();
		requiredDependencies = new HashMap<Integer, Map<String, String>>();
		defaultValueDependencies = new HashMap<Integer, Map<String, String>>();
		checkDependencies = new HashMap<Integer, Map<String, String>>();
	}

	@Autowired
	private ExpressionFactory expressionFactory;

	@Autowired
	private SurveyManager surveyManager;

	public void init() {
		List<Survey> surveys = surveyManager.getAll();
		for (Survey survey : surveys) {
			registerDependencies(survey);
		}
	}

	private void registerDependencies(Survey survey) {
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootEntityDefinitions = schema.getRootEntityDefinitions();
		for (EntityDefinition entityDefinition : rootEntityDefinitions) {
			registerDependencies(entityDefinition);
		}
	}

	private void registerDependencies(EntityDefinition entityDefinition) {
		List<NodeDefinition> childDefinitions = entityDefinition.getChildDefinitions();
		for (NodeDefinition nodeDefinition : childDefinitions) {
			registerDependencies(nodeDefinition, nodeDefinition.getRelevantExpression(), relevantDependencies);
			registerDependencies(nodeDefinition, nodeDefinition.getRequiredExpression(), requiredDependencies);

			if (nodeDefinition instanceof AttributeDefinition) {
				registerDependencies((AttributeDefinition) nodeDefinition);
			} else {
				registerDependencies((EntityDefinition) nodeDefinition);
			}
		}
	}

	private void registerDependencies(AttributeDefinition attributeDefinition) {
		List<AttributeDefault> attributeDefaults = attributeDefinition.getAttributeDefaults();
		for (AttributeDefault attributeDefault : attributeDefaults) {
			registerDependencies(attributeDefinition, attributeDefault.getCondition(), defaultValueDependencies);
			registerDependencies(attributeDefinition, attributeDefault.getExpression(), defaultValueDependencies);
		}
		List<Check> checks = attributeDefinition.getChecks();
		for (Check check : checks) {
			registerDependencies(attributeDefinition, check.getCondition(), checkDependencies);
			if (check instanceof ComparisonCheck) {
				registerDependencies(attributeDefinition, ((ComparisonCheck) check).getEqualsExpression(), checkDependencies);
				registerDependencies(attributeDefinition, ((ComparisonCheck) check).getLessThanExpression(), checkDependencies);
				registerDependencies(attributeDefinition, ((ComparisonCheck) check).getLessThanOrEqualsExpression(), checkDependencies);
				registerDependencies(attributeDefinition, ((ComparisonCheck) check).getGreaterThanExpression(), checkDependencies);
				registerDependencies(attributeDefinition, ((ComparisonCheck) check).getGreaterThanOrEqualsExpression(), checkDependencies);
			} else if (check instanceof CustomCheck) {
				registerDependencies(attributeDefinition, ((CustomCheck) check).getExpression(), checkDependencies);
			} else if (check instanceof DistanceCheck) {
				registerDependencies(attributeDefinition, ((DistanceCheck) check).getDestinationPointExpression(), checkDependencies);
				registerDependencies(attributeDefinition, ((DistanceCheck) check).getMaxDistanceExpression(), checkDependencies);
				registerDependencies(attributeDefinition, ((DistanceCheck) check).getMinDistanceExpression(), checkDependencies);
				registerDependencies(attributeDefinition, ((DistanceCheck) check).getSourcePointExpression(), checkDependencies);
			} else if (check instanceof UniquenessCheck) {
				registerDependencies(attributeDefinition, ((UniquenessCheck) check).getExpression(), checkDependencies);
			}
		}
	}

	private void registerDependencies(NodeDefinition nodeDefinition, String expression, Map<Integer, Map<String, String>> dependencies) {
		if (StringUtils.isNotBlank(expression)) {
			List<String> referencedPaths = getReferencedPaths(expression);
			for (String path : referencedPaths) {
				try {
					String normalizedPath = getNormalizedPath(path);
					SchemaPathExpression schemaExpression = new SchemaPathExpression(normalizedPath);
					EntityDefinition parentDefinition = nodeDefinition.getParentDefinition();
					NodeDefinition dependantNode = schemaExpression.evaluate(parentDefinition);

					String sourcePath = dependantNode.getPath();
					String destinationPath = nodeDefinition.getPath();
					String relativePath = getRelativePath(sourcePath, destinationPath);

					Integer surveyId = nodeDefinition.getSurvey().getId();
					Map<String, String> dependenciesMap = getDependenciesMap(dependencies, surveyId);
					dependenciesMap.put(sourcePath, relativePath);
				} catch (Exception e) {
					if (LOG.isErrorEnabled()) {
						LOG.error("Unable to register dependency for node " + nodeDefinition.getPath() + " with expression " + path, e);
					}
				}
			}
		}
	}

	private Map<String, String> getDependenciesMap(Map<Integer, Map<String, String>> dependencies, Integer surveyId) {
		Map<String, String> dependenciesMap = dependencies.get(surveyId);
		if (dependenciesMap == null) {
			dependenciesMap = new HashMap<String, String>();
			dependencies.put(surveyId, dependenciesMap);
		}
		return dependenciesMap;
	}

	private List<String> getReferencedPaths(String expression) {
		if (StringUtils.isBlank(expression)) {
			return Collections.emptyList();
		} else {
			try {
				ModelPathExpression pathExpression = expressionFactory.createModelPathExpression(expression);
				return pathExpression.getReferencedPaths();
			} catch (InvalidExpressionException e) {
				if (LOG.isErrorEnabled()) {
					LOG.error("Invalid expression " + expression, e);
				}
				return Collections.emptyList();
			}
		}
	}

	private String getRelativePath(String xpathSource, String xpathDestination) {
		String path = "";
		String[] sources = xpathSource.split("\\/");
		String[] dests = xpathDestination.split("\\/");
		int i = 0;
		for (; i < sources.length; i++) {
			String src = sources[i];
			String dest = dests[i];
			if (dest.equals(src)) {
				continue;
			} else {
				break;
			}
		}

		for (int k = i; k < sources.length; k++) {
			if (path != "")
				path += "/";
			path += "parent()";
		}

		for (int k = i; k < dests.length; k++) {
			if (path != "")
				path += "/";
			path += dests[k];
		}
		return path;
	}

	private String getNormalizedPath(String path) {
		return path.replaceAll("\\$this/", "");
	}

	public ExpressionFactory getExpressionFactory() {
		return expressionFactory;
	}

	public void setExpressionFactory(ExpressionFactory expressionFactory) {
		this.expressionFactory = expressionFactory;
	}

	public SurveyManager getSurveyManager() {
		return surveyManager;
	}

	public void setSurveyManager(SurveyManager surveyManager) {
		this.surveyManager = surveyManager;
	}
	
}
