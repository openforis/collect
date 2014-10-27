package org.openforis.idm.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * 
 * @author S. Ricci
 * @author D. Wiell
 *
 */
public abstract class DependencyGraph<T> {

	private static final Log log = LogFactory.getLog(RelevanceDependencyGraph.class);

	protected abstract boolean isDependentItemIncluded(T item);
	
	protected abstract Collection<T> toItems(Node<?> node);

	protected abstract Set<NodePathPointer> determineDependents(T source) throws InvalidExpressionException;

	protected abstract Set<NodePathPointer> determineSources(T dependent)	throws InvalidExpressionException;

	protected abstract List<T> getChildren(T item);

	protected abstract Object getId(T item);

	protected abstract String toString(T item);

	private final Map<Object, GraphNode> graphNodeById;

	public DependencyGraph(Survey survey) {
		super();
		this.graphNodeById = new HashMap<Object, GraphNode>();
	}

	public void add(Node<?> node) {
		Collection<T> items = toItems(node);
		for (T t : items) {
			addItem(t);
		}
	}

	private void addItem(T item) {
		try {
			Set<NodePathPointer> sourceNodePointers = determineSources(item);
			Set<T> sourceItems = getRelatedItems(item, sourceNodePointers);
			
			Set<NodePathPointer> dependentNodePointers = determineDependents(item);
			Set<T> dependentItems = getRelatedItems(item, dependentNodePointers);
			
			addDependencies(item, sourceItems, dependentItems);
		} catch (InvalidExpressionException e) {
			log.error(String.format("Error registering dependencies for node %s", toString(item)), e);
		}
	}

	private void addDependencies(T item, Set<T> sources, Set<T> dependents) {
		GraphNode graphNode = getOrCreateGraphNode(item);
		for (T source : sources) {
			GraphNode sourceGraphNode = getOrCreateGraphNode(source);
			graphNode.addSource(sourceGraphNode);
		}
		for (T dependent : dependents) {
			GraphNode dependentGraphNode = getOrCreateGraphNode(dependent);
			dependentGraphNode.addSource(graphNode);
		}
	}

	private GraphNode getOrCreateGraphNode(T item) {
		Object nodeId = getId(item);
		GraphNode graphNode = graphNodeById.get(nodeId);
		if (graphNode == null) {
			graphNode = new GraphNode(this, item);
			graphNodeById.put(nodeId, graphNode);
		}
		return graphNode;
	}

	public void remove(Node<?> node) {
		Collection<T> items = toItems(node);
		for (T item : items) {
			Object nodeId = getId(item);
			GraphNode graphNode = graphNodeById.get(nodeId);
			if ( graphNode != null ) {
				graphNodeById.remove(nodeId);
				for (GraphNode dependent : graphNode.dependents) {
					dependent.sources.remove(graphNode);
				}
				for (GraphNode source : graphNode.sources) {
					source.dependents.remove(graphNode);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public List<T> dependenciesFor(T item) {
		return dependenciesForItems(Arrays.asList(item));
	}

	public List<T> dependenciesFor(Collection<Node<?>> nodes) {
		Set<T> items = new HashSet<T>();
		for (Node<?> node : nodes) {
			items.addAll(toItems(node));
		}
		return dependenciesForItems(items);
	}
	
	protected List<T> dependenciesForItems(Collection<T> items) {
		Set<GraphNode> nodes = new HashSet<GraphNode>();
		for (T item : items) {
			Object id = getId(item);
			GraphNode graphNode = graphNodeById.get(id);
			if ( graphNode != null ) {
				nodes.add(graphNode);
			}
		}
		return getDependentNodes(nodes);
	}

	private List<T> getDependentNodes(Set<GraphNode> nodes) {
		Set<GraphNode> nodesToSort = new HashSet<GraphNode>(nodes);
		for (GraphNode graphNode : nodes) {
			nodesToSort.addAll(graphNode.getUnsortedDependents());
		}
		List<T> result = getSortedDependentItems(nodesToSort);
		
		Iterator<T> it = result.iterator();
		while ( it.hasNext() ) {
			T item = it.next();
			if ( ! isDependentItemIncluded(item) ) {
				it.remove();
			}
		}
		return result;
	}

	private Set<T> getRelatedItems(T item, Set<NodePathPointer> pointers) throws InvalidExpressionException {
		Set<T> result = new HashSet<T>();
		for (NodePathPointer nodePathPointer : pointers) {
			String entityPath = nodePathPointer.getEntityPath();
			if ( StringUtils.isBlank(entityPath) ) {
				result.addAll(determineRelatedItems(item, nodePathPointer.getChildName()));
			} else {
				result.addAll(determineRelatedItems(item, nodePathPointer.getChildName(), entityPath));
			}
		}
		return result;
	}

	protected abstract Set<T> determineRelatedItems(T item, String childName);

	protected abstract Set<T> determineRelatedItems(T item,
			String relatedChildName,
			String entityPath)
			throws InvalidExpressionException;
	
	protected List<T> extractItems(Collection<GraphNode> nodes) {
		List<T> result = new ArrayList<T>();
		for (GraphNode graphNode : nodes) {
			result.add(graphNode.item);
		}
		return result;
	}
	
	protected List<T> getSortedDependentItems(Set<GraphNode> toSort) {
		return extractItems(toSort);
	}

	class GraphNode {
		
		final DependencyGraph<T> graph;
		final T item;
		final Set<GraphNode> sources = new LinkedHashSet<GraphNode>();
		final Set<GraphNode> dependents = new LinkedHashSet<GraphNode>();
		
		public GraphNode(DependencyGraph<T> graph, T item) {
			this.graph = graph;
			this.item = item;
		}
		
		public void addSource(GraphNode node) {
			if ( node != this ) {
				sources.add(node);
				node.dependents.add(this);
			}
		}

		private Set<GraphNode> getUnsortedDependents() {
			return getUnsortedDependents(new HashSet<GraphNode>());
		}
		
		private Set<GraphNode> getUnsortedDependents(Set<GraphNode> visited) {
			if ( visited.contains(this) ) {
				return Collections.emptySet();
			}
			visited.add(this);
			
			Set<GraphNode> result = new HashSet<GraphNode>();
			for (GraphNode graphNode : dependents) {
				result.add(graphNode);
				result.addAll(graphNode.getUnsortedDependents(visited));
			}
			List<T> children = getChildren(item);
			for (T child : children) {
				GraphNode childGrapNode = graphNodeById.get(getId(child));
				result.add(childGrapNode);
				result.addAll(childGrapNode.getUnsortedDependents(visited));
			}
			return result;
		}
		
		@Override
		public String toString() {
			return DependencyGraph.this.toString(this.item);
		}

		public Object getItemId() {
			return graph.getId(item);
		}
		
	}

	/**
	 * 
	 * Refer to http://en.wikipedia.org/wiki/Topological_sorting for the details on the implemented algorithm.
	 *
	 */
	class GraphSorter {
		Set<Object> permanentlyMarkedNodeIds;
		Set<Object> temporarilyMarkedNodeIds;
		Set<GraphNode> nodes;
		Set<GraphNode> unmarkedNodes;
		LinkedList<T> results;
		
		public GraphSorter(Set<GraphNode> nodes) {
			this.nodes = nodes;
			this.results = new LinkedList<T>();
			this.permanentlyMarkedNodeIds = new LinkedHashSet<Object>(nodes.size());
			this.temporarilyMarkedNodeIds = new LinkedHashSet<Object>(nodes.size());
			this.unmarkedNodes = new LinkedHashSet<GraphNode>(this.nodes);
		}
		
		public List<T> sort() {
			while (! unmarkedNodes.isEmpty()) {
				GraphNode n = unmarkedNodes.iterator().next();
				visit(n);
			}
			return results;
		}

		private void visit(GraphNode n) {
			Object nodeId = n.getItemId();
			if ( temporarilyMarkedNodeIds.contains(nodeId) ) {
				throw new IllegalStateException(String.format("Circular dependency found in graph for node %s", n));
			}
			if ( ! permanentlyMarkedNodeIds.contains(nodeId) ) {
				temporarilyMarkedNodeIds.add(nodeId);
				for (GraphNode m : n.sources) {
					if (nodes.contains(m) ) {
						visit(m);	
					}
				}
				permanentlyMarkedNodeIds.add(nodeId);
				temporarilyMarkedNodeIds.remove(nodeId);
				unmarkedNodes.remove(n);
				results.add(n.item);
			}
		}
	}
}