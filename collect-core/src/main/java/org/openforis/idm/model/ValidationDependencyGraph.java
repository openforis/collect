package org.openforis.idm.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * 
 * @author S. Ricci
 * @author D. Wiell
 *
 */
public class ValidationDependencyGraph extends NodeDependencyGraph {
	
	public ValidationDependencyGraph(Survey survey) {
		super(survey);
	}

	@Override
	protected boolean isDependentItemIncluded(Node<?> node) {
		return node instanceof Attribute;
	}

	@Override
	protected List<Node<?>> getSortedDependentItems(GraphNode node, Set<GraphNode> unsortedDependents) {
		Set<GraphNode> nodes = new HashSet<GraphNode>(unsortedDependents);
		nodes.add(node);
		return extractItems(nodes);
	}

	@Override
	protected Set<NodePathPointer> determineDependents(Node<?> source) throws InvalidExpressionException {
		Survey survey = source.getSurvey();
		Set<NodePathPointer> dependentPointers = survey.getValidationDependencies(source.getDefinition());
		return dependentPointers;
	}

	@Override
	protected Set<NodePathPointer> determineSources(Node<?> dependent) throws InvalidExpressionException {
		Survey survey = dependent.getSurvey();
		Set<NodePathPointer> sourcePointers = survey.getValidationSources(dependent.getDefinition());
		return sourcePointers;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Set<Attribute<?, ?>> dependentAttributes(Collection<Node<?>> nodes) {
		List dependencies = super.dependenciesFor(nodes);
		return new HashSet(dependencies);
	}
	
}
