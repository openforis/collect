/**
 * 
 */
package org.openforis.collect.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.expression.SchemaPathExpression;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.InvalidExpressionException;
import org.openforis.idm.model.expression.ModelPathExpression;

/**
 * @author M. Togna
 * 
 */
public class StateDependencyMap {

	private static final Log LOG = LogFactory.getLog(StateDependencyMap.class);

	private Map<String, Set<String>> dependencies;
	private ExpressionFactory expressionFactory;

	public StateDependencyMap(ExpressionFactory expressionFactory) {
		this.expressionFactory = expressionFactory;
		dependencies = new HashMap<String, Set<String>>();
	}

	public Set<String> getDependantPaths(String path) {
		Set<String> set = dependencies.get(path);
		if (set != null) {
			return set;
		} else {
			return Collections.emptySet();
		}
	}

	public void register(NodeDefinition nodeDefinition, String expression) {
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

					register(sourcePath, relativePath);
				} catch (Exception e) {
					if (LOG.isErrorEnabled()) {
						LOG.error("Unable to register dependency for node " + nodeDefinition.getPath() + " with expression " + path, e);
					}
				}
			}
		}
	}

	private void register(String nodePath, String dependantNodePath) {
		Set<String> set = dependencies.get(nodePath);
		if (set == null) {
			set = new HashSet<String>();
			dependencies.put(nodePath, set);
		}
		set.add(dependantNodePath);
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

}
