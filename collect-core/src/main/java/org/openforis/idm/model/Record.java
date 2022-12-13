/**
 * 
 */
package org.openforis.idm.model;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openforis.commons.collection.ItemAddVisitor;
import org.openforis.commons.collection.Visitor;
import org.openforis.commons.lang.DeepComparable;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.expression.InvalidExpressionException;
import org.openforis.idm.path.Path;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 * @author D. Wiell
 */
public class Record implements DeepComparable {

	private Map<Integer, Node<? extends NodeDefinition>> nodesByInternalId;
	private Survey survey;

	private Integer id;
	private ModelVersion modelVersion;
	private int nextId;
	private Entity rootEntity;
	private Integer rootEntityDefinitionId;
	
	boolean dependencyGraphsEnabled = true;
	boolean validationDependencyGraphsEnabled = true;
	boolean ignoreDuplicateRecordKeyValidationErrors = false;
	NodeDependencyGraph calculatedAttributeDependencies;
	RelevanceDependencyGraph relevanceDependencies;
	MinCountDependencyGraph minCountDependencies;
	MaxCountDependencyGraph maxCountDependencies;
	ValidationDependencyGraph validationDependencies;
	CodeAttributeDependencyGraph codeAttributeDependencies;

	List<DependencyGraph<?>> dependencyGraphs;
	
	public Record(Survey survey, String version, String rootEntityDefName) {
		this(survey, version, survey.getSchema().getRootEntityDefinition(rootEntityDefName));
	}
	
	public Record(Survey survey, String version, EntityDefinition rootEntityDefinition) {
		this(survey, version, rootEntityDefinition, true, true, false);
	}
	
	public Record(Survey survey, String version, EntityDefinition rootEntityDefinition, 
			boolean dependencyGraphsEnabled,
			boolean enableValidationDependencyGraphs, 
			boolean ignoreDuplicateRecordKeyValidationErrors) {
		if (survey == null) {
			throw new IllegalArgumentException("Survey required");
		}
		this.survey = survey;
		if (version == null) { //default to latest version
			if (survey.getVersions() != null) {
				this.modelVersion = survey.getLatestVersion();
			}
		} else {
			this.modelVersion = survey.getVersion(version);
			if (modelVersion == null) {
				throw new IllegalArgumentException(String.format("Invalid version name: %s", version));
			}
		}
		if (rootEntityDefinition == null) {
			rootEntityDefinition = survey.getSchema().getFirstRootEntityDefinition();
		}
		this.dependencyGraphsEnabled = dependencyGraphsEnabled;
		this.validationDependencyGraphsEnabled = enableValidationDependencyGraphs;
		this.ignoreDuplicateRecordKeyValidationErrors = ignoreDuplicateRecordKeyValidationErrors;
		reset();
		setRootEntity((Entity) rootEntityDefinition.createNode());
	}

	protected Entity createRootEntity(EntityDefinition def) {
		if (rootEntity != null) {
			throw new IllegalStateException(
					"Record already has an associated root entity");
		}
		setRootEntity((Entity) def.createNode());
		return rootEntity;
	}

	public void replaceRootEntity(Entity rootEntity) {
		reset();
		setRootEntity(rootEntity);
	}

	private void setRootEntity(Entity entity) {
		this.rootEntity = entity;
		this.rootEntityDefinitionId = entity.getDefinition().getId();
		put(rootEntity);
	}

	protected void reset() {
		this.nodesByInternalId = new HashMap<Integer, Node<? extends NodeDefinition>>();
		this.nextId = 1;
		resetValidationDependencies();
	}

	@SuppressWarnings("unchecked")
	protected void resetValidationDependencies() {
		this.calculatedAttributeDependencies = new CalculatedAttributeDependencyGraph(survey);
		this.minCountDependencies = new MinCountDependencyGraph(survey);
		this.maxCountDependencies = new MaxCountDependencyGraph(survey);
		this.relevanceDependencies = new RelevanceDependencyGraph(survey);
		this.validationDependencies = new ValidationDependencyGraph(survey);
		this.codeAttributeDependencies = new CodeAttributeDependencyGraph(survey);
		
		this.dependencyGraphs = Arrays.asList(
				calculatedAttributeDependencies,
				minCountDependencies,
				maxCountDependencies,
				relevanceDependencies,
				validationDependencies,
				codeAttributeDependencies
				);
	}

	/**
	 * Looks for a node in the specified path and throws an Exception if the node is not found.
	 * 
	 * @param path Node path
	 * @return Node
	 * @throws IllegalArgumentException if the node is not found.
	 */
	public <N extends Node<?>> N getNodeByPath(String path) {
		N node = findNodeByPath(path);
		if (node == null) {
			throw new IllegalArgumentException(
					String.format("Could not find node %s in survey %d record %d ", 
							path, getSurvey().getId(), getId()));
		}
		return node;
	}
	
	public <N extends Node<?>> N findNodeByPath(String path) {
		List<Node<?>> nodes = findNodesByPath(path);
		if ( nodes.size() == 0 ) {
			return null;
		} else if ( nodes.size() == 1 ) {
			@SuppressWarnings("unchecked")
			N n = (N) nodes.get(0);
			return n;
		} else {
			throw new IllegalArgumentException(
					"Multiple nodes found for path: " + path);
		}
	}

	/**
	 * @param path Nodes path
	 * @return List of nodes
	 * @deprecated Use {@link #findNodesByPath(String)} instead
	 */
	@Deprecated
	public <N extends Node<?>> List<N> getNodesByPath(String path) {
		return findNodesByPath(path);
	}
	
	public <N extends Node<?>> List<N> findNodesByPath(String path) {
		Path p = Path.parse(path);
		@SuppressWarnings("unchecked")
		List<N> result = (List<N>) p.evaluate(this);
		return result;
	}

	@SuppressWarnings("unchecked")
	public <N extends Node<?>> List<N> findNodesByExpression(String expression) {
		try {
			return (List<N>) getSurveyContext().getExpressionEvaluator()
					.evaluateNodes(getRootEntity(), getRootEntity(), expression);
		} catch (InvalidExpressionException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Clear all node states
	 */
	public void clearNodeStates() {
		rootEntity.traverse(new NodeVisitor() {
			@Override
			public void visit(Node<? extends NodeDefinition> node, int idx) {
				if (node instanceof Attribute) {
					Attribute<?, ?> attribute = (Attribute<?, ?>) node;
					attribute.clearFieldStates();
				} else if (node instanceof Entity) {
					Entity entity = (Entity) node;
					entity.clearChildStates();
				}
			}
		});
	}

	public int countNodes() {
		return nodesByInternalId.size();
	}
	
	/**
	 * @return the percentage of all filled relevant attributes among the required ones
	 */
	public int calculateCompletionPercent() {
		if (getRootEntity() == null) {
			return -1;
		}
		int totalRequiredAttributes = 0;
		int filledAttributes = 0;
		Deque<Node<?>> stack = new LinkedList<Node<?>>();
		stack.push(getRootEntity());
		while (! stack.isEmpty()) {
			Node<?> node = stack.pop();
			if (node.isRelevant()) {
				if (node instanceof Attribute) {
					if (node.isRequired()) {
						totalRequiredAttributes ++;
						if (! node.isEmpty()) {
							filledAttributes ++;
						}
					}
				} else {
					stack.addAll(((Entity) node).getChildren());
				}
			}
		}
		if (totalRequiredAttributes == 0) {
			return -1;
		} else {
			return Double.valueOf(Math.floor((double) (100 * filledAttributes / totalRequiredAttributes))).intValue();
		}
	}
	
	public int countTotalFilledAttributes() {
		if (getRootEntity() == null) {
			return -1;
		}
		int total = 0;
		Deque<Node<?>> stack = new LinkedList<Node<?>>();
		stack.push(getRootEntity());
		while (! stack.isEmpty()) {
			Node<?> node = stack.pop();
			if (node.isRelevant()) {
				if (node instanceof Attribute) {
					if (! node.isEmpty()) {
						total ++;
					}
				} else {
					stack.addAll(((Entity) node).getChildren());
				}
			}
		}
		return total;
	}
	
	void put(Node<?> node) {
		initialize(node);
		if (dependencyGraphsEnabled) {
			if ( validationDependencyGraphsEnabled ) {
				registerInAllDependencyGraphs(node);
			} else {
				//register only calculated attribute dependencies
				registerInDependencyGraph(calculatedAttributeDependencies, node);
				registerInDependencyGraph(codeAttributeDependencies, node);
			}
		}
	}

	private void registerInDependencyGraph(final DependencyGraph<?> graph, Node<?> node) {
		if (node instanceof Entity) {
			((Entity) node).traverse(new NodeVisitor() {
				@Override
				public void visit(Node<? extends NodeDefinition> node, int idx) {
					graph.add(node);
				}
			});
		} else {
			graph.add(node);
		}
	}

	protected void registerInAllDependencyGraphs(Node<?> node) {
		for (DependencyGraph<?> graph : dependencyGraphs) {
			registerInDependencyGraph(graph, node);
		}
	}

	protected void initialize(Node<?> node) {
		int id = this.nextId();
		node.internalId = id;
		node.setRecord(this);
		node.detached = false;

		nodesByInternalId.put(id, node);

		if (node instanceof Entity) {
			for (Node<?> child : ((Entity) node).getChildren()) {
				initialize(child);
			}
		}
	}

	protected void remove(Node<?> node) {
		node.parent = null;
		node.setRecord(null);
		node.detached = true;
		nodesByInternalId.remove(node.internalId);

		for (DependencyGraph<?> graph : dependencyGraphs) {
			graph.remove(node);
		}
	}
	
	int nextId() {
		return nextId++;
	}
	
	public Node<?> getNodeByInternalId(int id) {
		return this.nodesByInternalId.get(id);
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public SurveyContext<?> getSurveyContext() {
		return survey.getContext();
	}

	public Survey getSurvey() {
		return this.survey;
	}

	public Entity getRootEntity() {
		return this.rootEntity;
	}
	
	public void setRootEntityDefinitionId(Integer rootEntityDefinitionId) {
		this.rootEntityDefinitionId = rootEntityDefinitionId;
	}
	
	public Integer getRootEntityDefinitionId() {
		return rootEntityDefinitionId;
	}

	public ModelVersion getVersion() {
		return this.modelVersion;
	}

	public boolean isDependencyGraphsEnabled() {
		return dependencyGraphsEnabled;
	}
	
	public void setDependencyGraphsEnabled(boolean dependencyGraphsEnabled) {
		this.dependencyGraphsEnabled = dependencyGraphsEnabled;
	}
	
	public boolean isIgnoreDuplicateRecordKeyValidationErrors() {
		return ignoreDuplicateRecordKeyValidationErrors;
	}
	
	public boolean isValidationDependencyGraphsEnabled() {
		return validationDependencyGraphsEnabled;
	}
	
	public void setValidationDependencyGraphsEnabled(boolean validationDependencyGraphsEnabled) {
		this.validationDependencyGraphsEnabled = validationDependencyGraphsEnabled;
	}
	
	public void setIgnoreDuplicateRecordKeyValidationErrors(boolean ignoreDuplicateRecordKeyValidationErrors) {
		this.ignoreDuplicateRecordKeyValidationErrors = ignoreDuplicateRecordKeyValidationErrors;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Attribute<?, ?>> determineCalculatedAttributes(Node<?> node) {
		List dependenciesFor = calculatedAttributeDependencies.dependenciesFor(node);
		return dependenciesFor;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Attribute<?, ?>> determineCalculatedAttributes(Set<Node<?>> nodes) {
		List dependenciesFor = calculatedAttributeDependencies.dependenciesFor(nodes);
		return dependenciesFor;
	}

	public <N extends Node<?>> List<NodePointer> determineRelevanceDependentNodes(Collection<N> nodes) {
		@SuppressWarnings("unchecked")
		List<NodePointer> result = relevanceDependencies.dependenciesFor((Collection<Node<?>>) nodes);
		return result;
	}
	
	private void visitDependencies(NodePointer nodePointer, Set<NodePathPointer> dependencies,
			Visitor<NodePointer> visitor) {
		Set<NodePathPointer> dependenciesInVersion = NodePathPointer.filterPointersByVersion(dependencies, nodePointer.getModelVersion());
		for (NodePathPointer nodePathPointer : dependenciesInVersion) {
			NodePointer nodePointerToVisit;
			String entityPath = nodePathPointer.getEntityPath();
			List<Node<?>> entities = Path.parse(entityPath).evaluate(nodePointer.getEntity());
			for (Node<?> entity: entities) {
				nodePointerToVisit = new NodePointer((Entity) entity, nodePathPointer.getReferencedNodeDefinition());
				visitor.visit(nodePointerToVisit);
			}
		}
	}
	
	public void visitDefaultValueDependencies(NodePointer nodePointer, Visitor<NodePointer> visitor) {
		Set<NodePathPointer> dependencies = survey.getDefaultValueDependencies(nodePointer.getChildDefinition());
		visitDependencies(nodePointer, dependencies, visitor);
	}
	
	public void visitDefaultValueDependenciesAndSelf(NodePointer nodePointer, Visitor<NodePointer> visitor) {
		if (nodePointer.getChildDefinition() instanceof AttributeDefinition) {
			visitor.visit(nodePointer);
		}
		visitDefaultValueDependencies(nodePointer, visitor);
	}
		
	public void visitRelevanceDependencies(NodePointer nodePointer, Visitor<NodePointer> visitor) {
		Set<NodePathPointer> relevanceDependencies = survey.getRelevanceDependencies(nodePointer.getChildDefinition());
		visitDependencies(nodePointer, relevanceDependencies, visitor);
	}
	
	public void visitRelevanceDependenciesAndSelf(NodePointer nodePointer, Visitor<NodePointer> visitor) {
		visitor.visit(nodePointer);
		visitRelevanceDependencies(nodePointer, visitor);
	}
	
	
	public void visitDependenciesThatRequireUpdates(NodePointer nodePointer, Visitor<NodePointer> visitor) {
		visitDefaultValueDependencies(nodePointer, visitor);
		visitRelevanceDependencies(nodePointer, visitor);
	}
	
	public Set<NodePointer> determineDependenciesThatRequireUpdates(Collection<Node<?>> nodes) {
		final Set<NodePointer> result = new LinkedHashSet<NodePointer>();
		
		Set<NodePointer> nodePointers = NodePointers.nodesToPointers(nodes);
		for (NodePointer nodePointer : nodePointers) {
			visitDependenciesThatRequireUpdates(nodePointer, new ItemAddVisitor<NodePointer>(result));
		}
		return result;
	}
	
	public List<NodePointer> determineRelevanceDependentNodePointers(Collection<NodePointer> nodePointers) {
		List<NodePointer> result = relevanceDependencies.dependenciesForPointers(nodePointers);
		return result;
	}

	public Collection<NodePointer> determineMinCountDependentNodes(Collection<NodePointer> nodePointers) {
		Collection<NodePointer> result = minCountDependencies.dependenciesForNodePointers(nodePointers);
		return result;
	}

	public Collection<NodePointer> determineMaxCountDependentNodes(Collection<NodePointer> nodePointers) {
		Collection<NodePointer> result = maxCountDependencies.dependenciesForNodePointers(nodePointers);
		return result;
	}

	public Set<Attribute<?, ?>> determineValidationDependentNodes(Collection<Node<?>> nodes) {
		Set<Attribute<?, ?>> result = validationDependencies.dependentAttributes(nodes);
		return result;
	}
	
	public Set<CodeAttribute> determineDependentCodeAttributes(NodePointer nodePointer) {
		return codeAttributeDependencies.dependentCodeAttributes(nodePointer);
	}
	
	public Set<CodeAttribute> determineDependentCodeAttributes(CodeAttribute codeAttr) {
		Set<CodeAttribute> result = codeAttributeDependencies.dependentCodeAttributes(codeAttr);
		return result;
	}
	
	public CodeAttribute determineParentCodeAttribute(CodeAttribute codeAttr) {
		CodeAttribute result = codeAttributeDependencies.parentCodeAttribute(codeAttr);
		return result; 
	}
	
	@Override
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Record other = (Record) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (modelVersion == null) {
			if (other.modelVersion != null)
				return false;
		} else if (!modelVersion.deepEquals(other.modelVersion))
			return false;
		if (rootEntity == null) {
			if (other.rootEntity != null)
				return false;
		} else if (!rootEntity.deepEquals(other.rootEntity))
			return false;
		if (survey == null) {
			if (other.survey != null)
				return false;
		//only very simple equals for survey
		} else if (!survey.equals(other.survey))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Record other = (Record) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringWriter sw = new StringWriter();
		sw.append("id: ").append(String.valueOf(id)).append("\n");
		if (rootEntity != null) {
			rootEntity.write(sw, 0);
		}
		return sw.toString();
	}
}
