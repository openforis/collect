package org.openforis.idm.model;

import java.util.Collection;
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
public class MinCountDependencyGraph extends NodePointerDependencyGraph {
	
	public MinCountDependencyGraph(Survey survey) {
		super(survey);
	}

	@Override
	protected Set<NodePathPointer> determineSources(NodePointer dependent) throws InvalidExpressionException {
		NodeDefinition def = dependent.getChildDefinition();
		Survey survey = def.getSurvey();
		Set<NodePathPointer> sourcePointers = survey.getMinCountSources(def);
		return sourcePointers;
	}
	
	@Override
	protected Set<NodePathPointer> determineDependents(NodePointer source) throws InvalidExpressionException {
		NodeDefinition def = source.getChildDefinition();
		Survey survey = def.getSurvey();
		Set<NodePathPointer> dependentPointers = survey.getMinCountDependencies(def);
		return dependentPointers;
	}

	@Override
	protected boolean isDependentItemIncluded(NodePointer node) {
		NodeDefinition nodeDef = node.getChildDefinition();
		return StringUtils.isNotBlank(nodeDef.getMinCountExpression());
	}

	public Collection<NodePointer> dependenciesForNodePointers(Collection<NodePointer> nodePointers) {
		return super.dependenciesForItems(nodePointers);
	}

}
