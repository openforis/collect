package org.openforis.idm.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.commons.collection.ItemAddVisitor;
import org.openforis.commons.collection.Visitor;
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

	private static final Logger LOG = LogManager.getLogger(RelevanceDependencyGraph.class);

	protected abstract boolean isDependentItemIncluded(T item);
	
	protected abstract Collection<T> toItems(Node<?> node);

	protected abstract Set<NodePathPointer> determineDependents(T source) throws InvalidExpressionException;

	protected abstract Set<NodePathPointer> determineSources(T dependent) throws InvalidExpressionException;

	protected abstract List<T> getChildren(T item);

	protected abstract Comparable<?> getId(T item);

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
			final GraphNode graphNode = getOrCreateGraphNode(item);

			//add graph node dependencies
			
			Set<NodePathPointer> sourceNodePointers = determineSources(item);
			visitRelatedItems(item, sourceNodePointers, new Visitor<T>() {
				public void visit(T source) {
					GraphNode sourceGraphNode = getOrCreateGraphNode(source);
					graphNode.addSource(sourceGraphNode);
				}
			});

			Set<NodePathPointer> dependentNodePointers = determineDependents(item);
			visitRelatedItems(item, dependentNodePointers, new Visitor<T>() {
				public void visit(T dependent) {
					GraphNode dependentGraphNode = getOrCreateGraphNode(dependent);
					dependentGraphNode.addSource(graphNode);
				};
			});
		} catch (InvalidExpressionException e) {
			LOG.error(String.format("Error registering dependencies for node %s", toString(item)), e);
		}
	}

	private GraphNode getGraphNodeByItem(T item) {
		return getGraphNode(getId(item));
	}
	
	private GraphNode getGraphNode(Comparable<?> nodeId) {
		return graphNodeById.get(nodeId);
	}
	
	private GraphNode getOrCreateGraphNode(T item) {
		GraphNode existingGraphNode = getGraphNodeByItem(item);
		if (existingGraphNode == null) {
			GraphNode graphNode = new GraphNode(this, item);
			Comparable<?> graphNodeId = getId(item);
			graphNodeById.put(graphNodeId, graphNode);
			return graphNode;
		} else {
			return existingGraphNode;
		}
	}

	public void remove(Node<?> node) {
		Collection<T> items = toItems(node);
		for (T item : items) {
			Comparable<?> graphNodeId = getId(item);
			GraphNode graphNode = getGraphNode(graphNodeId);
			if ( graphNode != null ) {
				graphNodeById.remove(graphNodeId);
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
		final Set<GraphNode> nodesToSort = new HashSet<GraphNode>();
		traverseDependentsUnsorted(nodes, new GraphNodeVisitor() {
			@SuppressWarnings("unchecked")
			public void visit(DependencyGraph<?>.GraphNode graphNode) {
				nodesToSort.add((DependencyGraph<T>.GraphNode) graphNode);
			}
		});
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
	
	private void traverseDependentsUnsorted(Collection<GraphNode> startFromNodes, GraphNodeVisitor visitor) {
		Deque<GraphNode> stack = new LinkedList<GraphNode>();
		Set<GraphNode> visited = new HashSet<GraphNode>();
		stack.addAll(startFromNodes);
		while(! stack.isEmpty()) {
			GraphNode graphNode = stack.pop();
			if (visited.add(graphNode)) {
				visitor.visit(graphNode);
				stack.addAll(graphNode.dependents);
				List<T> children = getChildren(graphNode.item);
				for (T child : children) {
					GraphNode childGraphNode = getGraphNodeByItem(child);
					if (childGraphNode == null) {
						//child node must have been already added to the graph during node initialization
						throw new IllegalStateException("Graph node not found for item: " + child);
					}
					stack.add(childGraphNode);
				}
			}
		}
	}
	
	protected List<T> sourcesForItem(T item) {
		return sourcesForItem(item, false);
	}
	
	protected List<T> sourcesForItem(T item, boolean sort) {
		GraphNode graphNode = getGraphNodeByItem(item);
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
	
	private void visitRelatedItems(T item, Set<NodePathPointer> pointers, Visitor<T> visitor) throws InvalidExpressionException {
		for (NodePathPointer nodePathPointer : pointers) {
			String entityPath = nodePathPointer.getEntityPath();
			if ( StringUtils.isBlank(entityPath) ) {
				visitRelatedItems(item, nodePathPointer.getReferencedNodeDefinition(), visitor);
			} else {
				visitRelatedItems(item, nodePathPointer.getReferencedNodeDefinition(), entityPath, visitor);
			}
		}
	}
	
	protected Set<T> determineRelatedItems(T item, NodeDefinition childDef) {
		Set<T> result = new HashSet<T>();
		visitRelatedItems(item, childDef, new ItemAddVisitor<T>(result));
		return result;
	}
	
	protected abstract void visitRelatedItems(T item, NodeDefinition childDef, Visitor<T> visitor);

	protected Set<T> determineRelatedItems(T item,
			NodeDefinition relatedChildDef,
			String entityPath)
			throws InvalidExpressionException {
		Set<T> result = new HashSet<T>();
		visitRelatedItems(item, relatedChildDef, entityPath, new ItemAddVisitor<T>(result));
		return result;
	}
	
	protected abstract void visitRelatedItems(T item,
			NodeDefinition relatedChildDef,
			String entityPath, Visitor<T> visitor)
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
			GraphNode graphNode = getGraphNodeByItem(item);
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

		private Set<GraphNode> getUnsortedSources() {
			Set<GraphNode> result = new LinkedHashSet<GraphNode>();
			Deque<GraphNode> stack = new LinkedList<GraphNode>();
			stack.addAll(this.sources);
			while (! stack.isEmpty()) {
				GraphNode node = stack.pop();
				if (! result.add(node)) {
					stack.addAll(node.sources);
				}
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
	
	interface GraphNodeVisitor extends Visitor<DependencyGraph<?>.GraphNode> {
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