package org.openforis.idm.model;

import java.util.List;
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
public class RelevanceDependencyGraph extends NodePointerDependencyGraph {
	
	public RelevanceDependencyGraph(Survey survey) {
		super(survey);
	}
	
	@Override
	protected List<NodePointer> getSortedDependentItems(Set<GraphNode> toSort) {
		List<NodePointer> result = new GraphSorter(toSort).sort();
		return result;
	}

	@Override
	protected Set<NodePathPointer> determineSources(NodePointer dependent) throws InvalidExpressionException {
		NodeDefinition def = dependent.getChildDefinition();
		Survey survey = def.getSurvey();
		Set<NodePathPointer> relevanceSources = survey.getRelevanceSources(def);
		return filterByVersion(relevanceSources, dependent.getModelVersion());
	}
	
	@Override
	protected Set<NodePathPointer> determineDependents(NodePointer source) throws InvalidExpressionException {
		NodeDefinition def = source.getChildDefinition();
		Survey survey = def.getSurvey();
		Set<NodePathPointer> relevanceDependencies = survey.getRelevanceDependencies(def);
		return filterByVersion(relevanceDependencies, source.getModelVersion());
	}

	@Override
	protected boolean isDependentItemIncluded(NodePointer node) {
		NodeDefinition nodeDef = node.getChildDefinition();
		return StringUtils.isNotBlank(nodeDef.getRelevantExpression());
	}

}
