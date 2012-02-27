package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.RecordContext;
import org.openforis.idm.model.state.ModelDependencies;
import org.openforis.idm.model.state.NodeState;

/**
 * @author G. Miceli
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

	private Step step;
	// TODO Replace submitted flag with state enum
	private boolean submitted;

	private Date creationDate;
	private User createdBy;
	private Date modifiedDate;
	private User modifiedBy;
	private Integer missing;
	private Integer skipped;
	private Integer errors;
	private Integer warnings;
	private Integer submittedId;
	
	private List<String> rootEntityKeys;
	private List<Integer> entityCounts;
	private Map<Integer, NodeState> nodeStateMap;
	private CollectSurvey collectSurvey;

	public CollectRecord(RecordContext context, CollectSurvey survey, String versionName) {
		super(context, survey, versionName);
		this.collectSurvey = survey;
		this.step = Step.ENTRY;
		this.submitted = false;

		// use List to preserve the order of the keys and counts
		rootEntityKeys = new ArrayList<String>();
		entityCounts = new ArrayList<Integer>();
		nodeStateMap = new HashMap<Integer, NodeState>();

	}

	public NodeState getNodeState(Node<?> node) {
		int nodeInternalId = node.getInternalId();
		NodeState nodeState = nodeStateMap.get(nodeInternalId);
		if(nodeState == null){
			nodeState = updateNodeStateInternal(node);
		}
		return nodeState;
	}

	/**
	 * <ol>
	 * <li>Change value of attribute x</li>
	 * <li>Update relevance states of all nodes R which depend on x for relevance and their descendants</li>
	 * <li>Update required states of all nodes R, of all nodes V which depend on value of x</li>
	 * <li>Revalidate all nodes R, V and x</li>
	 * </ol>
	 * 
	 * @param node
	 * @return
	 */
	public List<NodeState> updateNodeState(Node<?> node) {
		List<NodeState> nodeStates = new ArrayList<NodeState>();
		Set<Integer> ids = new HashSet<Integer>();
		updateNodeStateInternal(node, ids, nodeStates);
		return nodeStates;
	}

	public List<NodeState> deleteNodeState(Node<?> node) {
		List<NodeState> nodeStates = new ArrayList<NodeState>();
		Set<Integer> ids = new HashSet<Integer>();
		refreshDependentNodesState(node, ids, nodeStates);
		return nodeStates;
	}

	private void refreshDependentNodesState(Node<?> node, Set<Integer> updatedNodeIds, List<NodeState> nodeStates) {
		ModelDependencies dependencies = collectSurvey.getModelDependencies();
		Set<Node<?>> dependentNodes = dependencies.getDependantNodes(node);
		for (Node<?> dependentNode : dependentNodes) {
			updateNodeStateInternal(dependentNode, updatedNodeIds, nodeStates);
		}
	}

	private void updateNodeStateInternal(Node<?> node, Set<Integer> updatedNodeIds, List<NodeState> nodeStates) {
		Integer nodeId = node.getInternalId();
		if (!updatedNodeIds.contains(nodeId)) {
			NodeState nodeState = updateNodeStateInternal(node);
			nodeStates.add(nodeState);
			updatedNodeIds.add(nodeId);

			refreshDependentNodesState(node, updatedNodeIds, nodeStates);
		}
	}

	private NodeState updateNodeStateInternal(Node<?> node) {
		NodeState nodeState = new NodeState(node);
		nodeState.update(getValidator());
		nodeStateMap.put(node.getInternalId(), nodeState);
		return nodeState;
	}

	private Validator getValidator() {
		RecordContext context = getContext();
		Validator validator = context.getValidator();
		return validator;
	}

	public void setSubmitted(boolean submitted) {
		this.submitted = submitted;
	}

	public boolean isSubmitted() {
		return submitted;
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
	
	public Integer getSubmittedId() {
		return submittedId;
	}
	
	public void setSubmittedId(Integer submittedId) {
		this.submittedId = submittedId;
	}

}
