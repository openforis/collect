package org.openforis.idm.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * 
 * @author S. Ricci
 * @author D. Wiell
 *
 */
public class MaxCountDependencyGraph extends NodePointerDependencyGraph {
	
	public MaxCountDependencyGraph(Survey survey) {
		super(survey);
	}

	@Override
	protected Set<NodePathPointer> determineSources(NodePointer dependent) throws InvalidExpressionException {
		NodeDefinition def = dependent.getChildDefinition();
		if (hasVariableMaxCountExpression(def)) {
			Survey survey = def.getSurvey();
			Set<NodePathPointer> sourcePointers = survey.getMaxCountSources(def);
			return filterByVersion(sourcePointers, dependent.getModelVersion());
		} else {
			return Collections.emptySet();
		}
	}
	
	@Override
	protected Set<NodePathPointer> determineDependents(NodePointer source) throws InvalidExpressionException {
		NodeDefinition def = source.getChildDefinition();
		Survey survey = def.getSurvey();
		Set<NodePathPointer> dependentPointers = survey.getMaxCountDependencies(def);
		return filterByVersion(dependentPointers, source.getModelVersion());
	}

	@Override
	protected boolean isDependentItemIncluded(NodePointer node) {
		NodeDefinition nodeDef = node.getChildDefinition();
		return hasVariableMaxCountExpression(nodeDef);
	}

	private boolean hasVariableMaxCountExpression(NodeDefinition nodeDef) {
		return StringUtils.isNotBlank(nodeDef.getMaxCountExpression()) && nodeDef.getFixedMaxCount() == null;
	}

	public Collection<NodePointer> dependenciesForNodePointers(Collection<NodePointer> nodePointers) {
		return super.dependenciesForItems(nodePointers);
	}
}
