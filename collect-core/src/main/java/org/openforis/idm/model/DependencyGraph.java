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
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.idm.metamodel.NodeDefinition;
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
		return dependenciesForItems(itemsFromNodes(nodes));
	}
	
	protected List<T> dependenciesForItems(Collection<T> items) {
		return getDependentNodes(nodesFromItems(items));
	}

	private List<T> getDependentNodes(Collection<GraphNode> nodes) {
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

	protected List<T> sourcesForItem(T item) {
		return sourcesForItem(item, false);
	}
	
	protected List<T> sourcesForItem(T item, boolean sort) {
		Object graphNodeId = getId(item);
		GraphNode graphNode = graphNodeById.get(graphNodeId);
		if ( graphNode == null ) {
			return Collections.emptyList();
		} else if (sort) {
			return getSortedSourceItems(graphNode);
		} else {
			return getSourceItems(graphNode);
		}
	}

	protected List<T> getSortedSourceItems(GraphNode node) {
		Set<GraphNode> sourceGraphNodes = node.getUnsortedSources();
		return getSortedSourceItems(sourceGraphNodes);
	}

	protected List<T> getSourceItems(GraphNode node) {
		Set<GraphNode> sourceGraphNodes = node.getUnsortedSources();
		List<T> items = extractItems(sourceGraphNodes);
		return items;
	}
	
	private Set<T> getRelatedItems(T item, Set<NodePathPointer> pointers) throws InvalidExpressionException {
		Set<T> result = new HashSet<T>();
		for (NodePathPointer nodePathPointer : pointers) {
			String entityPath = nodePathPointer.getEntityPath();
			if ( StringUtils.isBlank(entityPath) ) {
				result.addAll(determineRelatedItems(item, nodePathPointer.getReferencedNodeDefinition()));
			} else {
				result.addAll(determineRelatedItems(item, nodePathPointer.getReferencedNodeDefinition(), entityPath));
			}
		}
		return result;
	}
	
	protected abstract Set<T> determineRelatedItems(T item, NodeDefinition childDef);

	protected abstract Set<T> determineRelatedItems(T item,
			NodeDefinition relatedChildDef,
			String entityPath)
			throws InvalidExpressionException;
	
	protected List<T> extractItems(Collection<GraphNode> nodes) {
		List<T> result = new ArrayList<T>(nodes.size());
		for (GraphNode graphNode : nodes) {
			result.add(graphNode.item);
		}
		return result;
	}
	
	private Collection<GraphNode> nodesFromItems(Collection<T> items) {
		Collection<GraphNode> nodes = new ArrayList<GraphNode>(items.size());
		for (T item : items) {
			Object id = getId(item);
			GraphNode graphNode = graphNodeById.get(id);
			if ( graphNode != null ) {
				nodes.add(graphNode);
			}
		}
		return nodes;
	}
	
	private Collection<T> itemsFromNodes(Collection<Node<?>> nodes) {
		Collection<T> result = new ArrayList<T>(nodes.size());
		for (Node<?> node : nodes) {
			result.addAll(toItems(node));
		}
		return result;
	}

	protected List<T> getSortedDependentItems(Set<GraphNode> toSort) {
		return extractItems(toSort);
	}

	protected List<T> getSortedSourceItems(Set<GraphNode> toSort) {
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

		private Collection<GraphNode> getUnsortedDependents() {
			Collection<GraphNode> result = new ArrayList<GraphNode>();
			Stack<GraphNode> stack = new Stack<GraphNode>();
			
			stack.addAll(this.getFirstLevelDependents());
			
			while (! stack.isEmpty()) {
				GraphNode node = stack.pop();
				if (! result.contains(node)) {
					result.add(node);
					stack.addAll(node.getFirstLevelDependents());
				}
			}
			return result;
		}
		
		private List<GraphNode> getFirstLevelDependents() {
			List<GraphNode> result = new ArrayList<GraphNode>();
			result.addAll(this.dependents);
			for (T child : getChildren(this.item)) {
				GraphNode childNode = getOrCreateGraphNode(child);
				result.add(childNode);
			}
			return result;
		}
		
		private Set<GraphNode> getUnsortedSources() {
			return getUnsortedSources(new LinkedHashSet<GraphNode>());
		}
		
		private Set<GraphNode> getUnsortedSources(Set<GraphNode> visited) {
			if ( visited.contains(this) ) {
				return Collections.emptySet();
			}
			visited.add(this);
			
			Set<GraphNode> result = new LinkedHashSet<GraphNode>();
			for (GraphNode graphNode : sources) {
				result.add(graphNode);
				result.addAll(graphNode.getUnsortedSources(visited));
			}
			return result;
		}
		
		public Object getItemId() {
			return graph.getId(item);
		}
		
		@Override
		public String toString() {
			return DependencyGraph.this.toString(this.item);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			Object itemId = getItemId();
			result = prime * result + ((itemId == null) ? 0 : itemId.hashCode());
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
			@SuppressWarnings("unchecked")
			GraphNode other = (GraphNode) obj;
			Object itemId = getItemId();
			Object otherItemId = other.getItemId();
			if (itemId == null) {
				if (otherItemId != null)
					return false;
			} else if (!itemId.equals(otherItemId))
				return false;
			return true;
		}

	}

	/**
	 * 
	 * Refer to http://en.wikipedia.org/wiki/Topological_sorting for the details on the implemented algorithm.
	 *
	 */
	class GraphSorter {
		
		LinkedList<T> results;
		
		Set<GraphNode> permanentlyMarkedNodes;
		Set<GraphNode> temporarilyMarkedNodes;
		Set<GraphNode> unmarkedNodes;
		
		private Set<GraphNode> nodes;

		public GraphSorter(Set<GraphNode> nodes) {
			this.nodes = nodes;
			this.permanentlyMarkedNodes = new LinkedHashSet<GraphNode>();
			this.temporarilyMarkedNodes = new LinkedHashSet<GraphNode>();
			this.unmarkedNodes = new LinkedHashSet<GraphNode>(this.nodes);
			this.results = new LinkedList<T>();
		}

		public List<T> sort() {
			while (! unmarkedNodes.isEmpty()) {
				GraphNode n = unmarkedNodes.iterator().next();
				visit(n);
			}
			return results;
		}

		private void visit(GraphNode n) {
			if ( temporarilyMarkedNodes.contains(n) ) {
				throw new IllegalStateException(String.format("Circular dependency found in graph for node %s", n));
			}
			if ( ! permanentlyMarkedNodes.contains(n) ) {
				temporarilyMarkedNodes.add(n);
				for (GraphNode m : n.sources) {
					if (nodes.contains(m) ) {
						visit(m);	
					}
				}
				permanentlyMarkedNodes.add(n);
				temporarilyMarkedNodes.remove(n);
				unmarkedNodes.remove(n);
				results.add(n.item);
			}
		}
	}
}