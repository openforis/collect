package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.state.ModelDependencies;
import org.openforis.idm.model.state.NodeState;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class CollectRecord extends Record {

	public enum Step {
		ENTRY(1), CLEANSING(2), ANALYSIS(3);

		private int stepNumber;

		private Step(int stepNumber) {
			this.stepNumber = stepNumber;
		}

		public int getStepNumber() {
			return stepNumber;
		}

		public static Step valueOf(int stepNumber) {
			Step[] values = Step.values();
			for (Step step : values) {
				if (step.getStepNumber() == stepNumber) {
					return step;
				}
			}
			return null;
		}
	}

	private transient Step step;

	private transient Date creationDate;
	private transient User createdBy;
	private transient Date modifiedDate;
	private transient User modifiedBy;
	private transient User lockedBy;
	private transient Integer missing;
	private transient Integer skipped;
	private transient Integer errors;
	private transient Integer warnings;
	
	private List<String> rootEntityKeys;
	private List<Integer> entityCounts;
	@Deprecated
	private Map<Integer, NodeState> nodeStateMap;

	public CollectRecord(CollectSurvey survey, String versionName) {
		super(survey, versionName);
		this.step = Step.ENTRY;
		
		// use List to preserve the order of the keys and counts
		rootEntityKeys = new ArrayList<String>();
		entityCounts = new ArrayList<Integer>();
		nodeStateMap = new HashMap<Integer, NodeState>();

	}
	@Deprecated
	public void updateNodeStates() {
		Entity entity = getRootEntity();
		updateAllRelevanceStates(entity);
		updateAllRequiredStates(entity);
		updateAllValidationStates(entity);
	}
	@Deprecated
	private void updateAllRelevanceStates(Node<?> node) {
		NodeState nodeState = getNodeStateInternal(node);
		nodeState.updateRelevance();

		if (node instanceof Entity) {
			Entity entity = (Entity) node;
			EntityDefinition definition = entity .getDefinition();
			List<NodeDefinition> childDefinitions = definition.getChildDefinitions();
			for (NodeDefinition childDefinition : childDefinitions) {
				String childName = childDefinition.getName();
				List<Node<? extends NodeDefinition>> children = entity.getAll(childName);
				for (Node<? extends NodeDefinition> child : children) {
					updateAllRelevanceStates(child);
				}
			}
		}
	}
	@Deprecated
	private void updateAllRequiredStates(Node<?> node) {
		NodeState nodeState = getNodeStateInternal(node);
		nodeState.updateRequired();

		if (node instanceof Entity) {
			Entity entity = (Entity) node;
			EntityDefinition definition = entity .getDefinition();
			List<NodeDefinition> childDefinitions = definition.getChildDefinitions();
			for (NodeDefinition childDefinition : childDefinitions) {
				String childName = childDefinition.getName();
				List<Node<? extends NodeDefinition>> children = entity.getAll(childName);
				for (Node<? extends NodeDefinition> child : children) {
					updateAllRequiredStates(child);
				}
			}
		}
	}
	
	@Deprecated
	private void updateAllValidationStates(Node<?> node) {
		NodeState nodeState = getNodeStateInternal(node);
		nodeState.updateValidation(getValidator());

		if (node instanceof Entity) {
			Entity entity = (Entity) node;
			EntityDefinition definition = entity .getDefinition();
			List<NodeDefinition> childDefinitions = definition.getChildDefinitions();
			for (NodeDefinition childDefinition : childDefinitions) {
				String childName = childDefinition.getName();
				List<Node<? extends NodeDefinition>> children = entity.getAll(childName);
				for (Node<? extends NodeDefinition> child : children) {
					updateAllValidationStates(child);
				}
			}
		}
	}
	
	@Deprecated
	private NodeState getNodeStateInternal(Node<?> child) {
		Integer internalId = child.getInternalId();
		NodeState nodeState = nodeStateMap.get(internalId);
		if (nodeState == null) {
			nodeState = new NodeState(child);
			nodeStateMap.put(internalId, nodeState);
		}
		return nodeState;
	}

	/**
	 * Returns a node states for the given node. It gets created if it doesn't exist.
	 * 
	 * @param node
	 * @return
	 */
	@Deprecated
	public NodeState getNodeState(Node<?> node) {
		int nodeInternalId = node.getInternalId();
		NodeState nodeState = nodeStateMap.get(nodeInternalId);
		if (nodeState == null) {
			nodeState = updateNodeStates(node);
			nodeStateMap.put(nodeInternalId, nodeState);
		}
		return nodeState;
	}

	@Deprecated
	public List<NodeState> updateNodeState(Node<?> node) {
		Set<Node<?>> nodesToRevalidate = new HashSet<Node<?>>();

		updateRelevanceAndDessendants(node, nodesToRevalidate);
		updateDependantRelevantNodes(node, nodesToRevalidate);
		
		Set<Node<?>> relevantNodes = new HashSet<Node<?>>();
		relevantNodes.addAll(nodesToRevalidate);
		for (Node<?> relNode : relevantNodes) {
			updateRequiredState(relNode, false, nodesToRevalidate);
		}
		updateRequiredState(node, true, nodesToRevalidate);

		List<NodeState> nodeStates = validate(nodesToRevalidate);

		return nodeStates;
	}
	
	@Deprecated
	private void updateDependantRelevantNodes(Node<?> node, Set<Node<?>> set) {
		ModelDependencies modelDependencies = null; //collectSurvey.getModelDependencies();
		Set<Node<?>> relevanceDependantNodes = modelDependencies.getRelevanceDependantNodes(node);
		for (Node<?> dependantNode : relevanceDependantNodes) {
			updateRelevanceAndDessendants(dependantNode, set);
		}
	}
	
	@Deprecated
	private List<NodeState> validate(Set<Node<?>> nodesToRevalidate) {
		List<NodeState> nodeStates = new ArrayList<NodeState>();
		for (Node<?> node : nodesToRevalidate) {
			NodeState nodeState = getNodeState(node);
			nodeState.updateValidation(getValidator());
			nodeStates.add(nodeState);
		}
		return nodeStates;
	}

	/**
	 * Update the relevance of a node and all its descendants Returns the set of the updated descendants
	 * 
	 * @param node
	 */
	@Deprecated
	private void updateRelevanceAndDessendants(Node<?> node, Set<Node<?>> set) {
		NodeState nodeState = getNodeState(node);
		nodeState.updateRelevance();
		set.add(node);

		if (node instanceof Entity) {
			Entity entity = (Entity) node;
			EntityDefinition entityDefinition = entity.getDefinition();
			List<NodeDefinition> childDefinitions = entityDefinition.getChildDefinitions();
			for (NodeDefinition childDefinition : childDefinitions) {
				String childName = childDefinition.getName();
				List<Node<?>> children = entity.getAll(childName);
				for (Node<?> childNode : children) {
					updateRelevanceAndDessendants(childNode, set);
				}
			}
		}
	}

	/**
	 * @param node
	 */
	@Deprecated
	private void updateRequiredState(Node<?> node, boolean updateDependants, Set<Node<?>> set) {
		NodeState nodeState = getNodeState(node);
		nodeState.updateRequired();
		set.add(node);

		if (updateDependants) {
			ModelDependencies dependencies = null;//collectSurvey.getModelDependencies();
			Set<Node<?>> nodes = dependencies.getRequiredDependantNodes(node);
			for (Node<?> dependantNode : nodes) {
				updateRequiredState(dependantNode, updateDependants, set);
			}
		}
	}
	
	@Deprecated
	public List<NodeState> deleteNodeState(Node<?> node) {
		Set<Node<?>> nodesToRevalidate = new HashSet<Node<?>>();

//		updateRelevanceAndDessendants(node, nodesToRevalidate);
		updateDependantRelevantNodes(node, nodesToRevalidate);
		
		Set<Node<?>> relevantNodes = new HashSet<Node<?>>();
		relevantNodes.addAll(nodesToRevalidate);
		for (Node<?> relNode : relevantNodes) {
			updateRequiredState(relNode, false, nodesToRevalidate);
		}
		updateRequiredState(node, true, nodesToRevalidate);

		List<NodeState> nodeStates = validate(nodesToRevalidate);

		return nodeStates;
	}
	@Deprecated
	private void deleteNodeState() {
		
	}

//	private void refreshDependentNodesState(Node<?> node, Set<Integer> updatedNodeIds, List<NodeState> nodeStates) {
//		ModelDependencies dependencies = collectSurvey.getModelDependencies();
//		Set<Node<?>> dependentNodes = dependencies.getDependantNodes(node);
//		for (Node<?> dependentNode : dependentNodes) {
//			updateNodeStateInternal(dependentNode, updatedNodeIds, nodeStates);
//		}
//	}

//	private void updateNodeStateInternal(Node<?> node, Set<Integer> updatedNodeIds, List<NodeState> nodeStates) {
//		Integer nodeId = node.getInternalId();
//		if (!updatedNodeIds.contains(nodeId)) {
//			NodeState nodeState = updateNodeStates(node);
//			nodeStates.add(nodeState);
//			updatedNodeIds.add(nodeId);
//
//			refreshDependentNodesState(node, updatedNodeIds, nodeStates);
//		}
//	}

	/**
	 * Update all states of a node
	 * 
	 * @param node
	 * @return
	 */
	@Deprecated
	private NodeState updateNodeStates(Node<?> node) {
		NodeState nodeState = new NodeState(node);
		nodeState.update(getValidator());
		nodeStateMap.put(node.getInternalId(), nodeState);
		return nodeState;
	}

	private Validator getValidator() {
		SurveyContext context = getSurveyContext();
		Validator validator = context.getValidator();
		return validator;
	}

	public Step getStep() {
		return step;
	}

	public void setStep(Step step) {
		this.step = step;
	}

	public Date getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public User getCreatedBy() {
		return this.createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public Date getModifiedDate() {
		return this.modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public User getModifiedBy() {
		return this.modifiedBy;
	}

	public void setModifiedBy(User modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Integer getSkipped() {
		return skipped;
	}

	public void setSkipped(Integer skipped) {
		this.skipped = skipped;
	}

	public Integer getMissing() {
		return missing;
	}

	public void setMissing(Integer missing) {
		this.missing = missing;
	}

	public Integer getErrors() {
		return errors;
	}

	public void setErrors(Integer errors) {
		this.errors = errors;
	}

	public Integer getWarnings() {
		return warnings;
	}

	public void setWarnings(Integer warnings) {
		this.warnings = warnings;
	}

	public List<String> getRootEntityKeys() {
		return rootEntityKeys;
	}

	public void setKeys(List<String> keys) {
		this.rootEntityKeys = keys;
	}

	public List<Integer> getEntityCounts() {
		return entityCounts;
	}

	public void setEntityCounts(List<Integer> counts) {
		this.entityCounts = counts;
	}

	public User getLockedBy() {
		return lockedBy;
	}
	
	public void setLockedBy(User lockedBy) {
		this.lockedBy = lockedBy;
	}
}
