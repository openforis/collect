package org.openforis.idm.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * 
 * @author S. Ricci
 * @author D. Wiell
 *
 */
public class CodeAttributeDependencyGraph extends NodeDependencyGraph {
	
	public CodeAttributeDependencyGraph(Survey survey) {
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
		if (source instanceof CodeAttribute) {
			Survey survey = source.getSurvey();
			Set<NodePathPointer> dependentPointers = survey.getRelatedCodeDependencies((CodeAttributeDefinition) source.getDefinition());
			return dependentPointers;
		} else {
			return Collections.emptySet();
		}
	}

	@Override
	protected Set<NodePathPointer> determineSources(Node<?> dependent) throws InvalidExpressionException {
		if (dependent instanceof CodeAttribute) {
			Survey survey = dependent.getSurvey();
			Set<NodePathPointer> sourcePointers = survey.getRelatedCodeSources((CodeAttributeDefinition) dependent.getDefinition());
			return sourcePointers;
		} else {
			return Collections.emptySet();
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Set<CodeAttribute> dependentCodeAttributes(CodeAttribute codeAttr) {
		List dependencies = super.dependenciesFor(codeAttr);
		//TODO check if the same node has to be included in the dependenciesFor method result
		dependencies.remove(codeAttr);
		return new HashSet(dependencies);
	}
	
	public CodeAttribute parentCodeAttribute(CodeAttribute codeAttr) {
		List<Node<?>> sources = sourcesForItem(codeAttr);
		if (sources.isEmpty()) {
			return null;
		} else {
			return (CodeAttribute) sources.get(0);
		}
	}
	
}
