/**
 * 
 */
package org.openforis.idm.model;

import java.io.StringWriter;
import java.util.ArrayList;
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

import org.openforis.commons.collection.ItemAddVisitor;
import org.openforis.commons.collection.Visitor;
import org.openforis.commons.lang.DeepComparable;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.Survey.DependencyType;
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
	
	boolean validationDependencyGraphsEnabled = true;
	boolean ignoreDuplicateRecordKeyValidationErrors = false;
	
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
		} else {
			@SuppressWarnings("unchecked")
			N n = (N) nodes.get(0);
			return n;
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
	
	private void visitNodePointerDependencies(NodePointer nodePointer, Set<NodePathPointer> dependencies,
			Visitor<NodePointer> visitor) {
		if (nodePointer.isNodesDeleted()) return;
		
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
	
	private Set<NodePointer> determineNodePointersDependentNodes(Collection<NodePointer> nodePointers, DependencyType dependencyType) {
		final Set<NodePointer> result = new LinkedHashSet<NodePointer>();
		for (NodePointer nodePointer : nodePointers) {
			visitNodePointerDependencies(nodePointer, dependencyType, new ItemAddVisitor<NodePointer>(result));
		}
		return result;
	}
	
	private void visitNodePointerDependencies(NodePointer nodePointer, DependencyType dependencyType, Visitor<NodePointer> visitor) {
		Set<NodePathPointer> dependencies = survey.getDependencies(nodePointer.getChildDefinition(), dependencyType);
		visitNodePointerDependencies(nodePointer, dependencies, visitor);
	}
	
	public void visitDefaultValueDependencies(NodePointer nodePointer, Visitor<NodePointer> visitor) {
		visitNodePointerDependencies(nodePointer, DependencyType.DEFAULT_VALUE, visitor);
	}
	
	public void visitDefaultValueDependenciesAndSelf(NodePointer nodePointer, Visitor<NodePointer> visitor) {
		if (nodePointer.getChildDefinition() instanceof AttributeDefinition) {
			visitor.visit(nodePointer);
		}
		visitDefaultValueDependencies(nodePointer, visitor);
	}
		
	public void visitRelevanceDependencies(NodePointer nodePointer, Visitor<NodePointer> visitor) {
		visitNodePointerDependencies(nodePointer, DependencyType.RELEVANCE, visitor);
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
	
	@SuppressWarnings("unchecked")
	private <T extends Node<?>> void visitNodeDependencies(Node<?> node, Set<NodePathPointer> dependencies, Visitor<T> visitor) {
		Set<NodePathPointer> dependenciesInVersion = NodePathPointer.filterPointersByVersion(dependencies, node.getModelVersion());
		for (NodePathPointer nodePathPointer : dependenciesInVersion) {
			String entityPath = nodePathPointer.getEntityPath();
			if (node.getParent() != null) {
				List<Node<?>> entities = Path.parse(entityPath).evaluate(node.getParent());
				for (Node<?> entity: entities) {
					List<Node<?>> children = ((Entity) entity).getChildren(nodePathPointer.getReferencedNodeDefinition());
					for (Node<?> child: children) {
						visitor.visit((T) child);
					}
				}
			}
		}
	}
	
	private <T extends Node<?>> void visitNodeDependencies(Node<?> node, DependencyType dependencyType, Visitor<T> visitor) {
		Set<NodePathPointer> dependencies = survey.getDependencies(node.getDefinition(), dependencyType);
		visitNodeDependencies(node, dependencies, visitor);
	}
	
	private <T extends Node<?>> Set<T> determineDependentNodes(Collection<Node<?>> nodes, DependencyType dependencyType) {
		final Set<T> result = new LinkedHashSet<T>();
		for (Node<?> node : nodes) {
			visitNodeDependencies(node, dependencyType, new ItemAddVisitor<T>(result));
		}
		return result;
	}
	
	public Set<NodePointer> determineMinCountDependentNodes(Collection<NodePointer> nodePointers) {
		return determineNodePointersDependentNodes(nodePointers, DependencyType.MIN_COUNT);
	}

	public Set<NodePointer> determineMaxCountDependentNodes(Collection<NodePointer> nodePointers) {
		return determineNodePointersDependentNodes(nodePointers, DependencyType.MAX_COUNT);
	}

	public Set<Attribute<?, ?>> determineValidationDependentNodes(Collection<Node<?>> nodes) {
		return determineDependentNodes(nodes, DependencyType.VALIDATION);
	}
	
	public Set<CodeAttribute> determineDependentCodeAttributes(NodePointer nodePointer) {
		return determineDependentNodes(nodePointer.getNodes(), DependencyType.PARENT_CODE);
	}
	
	public Set<CodeAttribute> determineDependentCodeAttributes(CodeAttribute codeAttr) {
		return determineDependentNodes(Arrays.<Node<?>>asList(codeAttr), DependencyType.PARENT_CODE);
	}
	
	public Set<CodeAttribute> determineDependentChildCodeAttributes(CodeAttribute codeAttr) {
		Set<CodeAttribute> dependentCodeAttributes = determineDependentCodeAttributes(codeAttr);
		Set<CodeAttribute> dependentChildCodeAttributes = new HashSet<CodeAttribute>(); 
		for (CodeAttribute dependentCodeAttribute : dependentCodeAttributes) {
			if  (dependentCodeAttribute.getCodeParent().equals(codeAttr)) {
				dependentChildCodeAttributes.add(dependentCodeAttribute);
			}
		}
		return dependentChildCodeAttributes;
	}
	
	public CodeAttribute determineParentCodeAttribute(CodeAttribute codeAttr) {
		final List<CodeAttribute> result = new ArrayList<CodeAttribute>();
		Set<NodePathPointer> sources = survey.getRelatedCodeSources(codeAttr.getDefinition());
		visitNodeDependencies(codeAttr, sources, new ItemAddVisitor<CodeAttribute>(result));
		if (result.isEmpty()) {
			return null;
		}
		CodeAttributeDefinition parentCodeAttributeDef = codeAttr.getDefinition().getParentCodeAttributeDefinition();
		for (CodeAttribute ancestorCodeAttr : result) {
			if (ancestorCodeAttr.getDefinition().equals(parentCodeAttributeDef)) {
				return ancestorCodeAttr;
			}
		}
		return null;
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
