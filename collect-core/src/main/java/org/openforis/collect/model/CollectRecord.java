package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodeVisitor;
import org.openforis.idm.model.NumberAttribute;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.TextAttribute;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class CollectRecord extends Record {

	private static final int APPROVED_MISSING_POSITION = 0;
	private static final int CONFIRMED_ERROR_POSITION = 0;
	private static final int DEFAULT_APPLIED_POSITION = 1;
	
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
		
		public Step getNext() {
			switch(this) {
				case ENTRY:
					return CLEANSING;
				case CLEANSING:
					return ANALYSIS;
				default:
					throw new IllegalArgumentException("This record cannot be promoted.");
			}
		}
		
		public Step getPrevious() {
			switch(this) {
				case CLEANSING:
					return Step.ENTRY;
				case ANALYSIS:
					return Step.CLEANSING;
				default:
					throw new IllegalArgumentException("This record cannot be promoted.");
			}
		}
		
	}

	public enum State {
		REJECTED("R");
		
		private String code;

		private State(String code) {
			this.code = code;
		}
		
		public String getCode() {
			return code;
		}
		
		public static State fromCode(String code) {
			State[] values = State.values();
			for (State state : values) {
				if (state.getCode().equals(code)) {
					return state;
				}
			}
			return null;
		}
	}
	
	private transient Step step;
	private transient State state;

	private transient Date creationDate;
	private transient User createdBy;
	private transient Date modifiedDate;
	private transient User modifiedBy;
	private transient Integer missing;
	private transient Integer missingErrors;
	private transient Integer missingWarnings;
	private transient Integer skipped;
	private transient Integer errors;
	private transient Integer warnings;
	
	private List<String> rootEntityKeyValues;
	private List<Integer> entityCounts;

	private Map<Integer, Set<String>> minCountErrorCounts;
	private Map<Integer, Set<String>> minCountWarningCounts;
	private Map<Integer, Set<String>> maxCountErrorCounts;
	private Map<Integer, Set<String>> maxCountWarningCounts;
	private Map<Integer, Integer> errorCounts;
	private Map<Integer, Integer> warningCounts;
	private Set<Integer> skippedNodes;
	
	public CollectRecord(CollectSurvey survey, String versionName) {
		super(survey, versionName);
		this.step = Step.ENTRY;
		
		// use List to preserve the order of the keys and counts
		rootEntityKeyValues = new ArrayList<String>();
		entityCounts = new ArrayList<Integer>();
		initErrorCountInfo();
	}

	private void initErrorCountInfo() {
		minCountErrorCounts = new HashMap<Integer, Set<String>>();
		minCountWarningCounts = new HashMap<Integer, Set<String>>();
		maxCountErrorCounts = new HashMap<Integer, Set<String>>();
		maxCountWarningCounts = new HashMap<Integer, Set<String>>();
		errorCounts = new HashMap<Integer, Integer>();
		warningCounts = new HashMap<Integer, Integer>();
		skippedNodes = new HashSet<Integer>();
		skipped = null;
		missing = null;
		missingErrors = null;
		missingWarnings = null;
		errors = null;
		warnings = null;
	}

	public Node<?> deleteNode(Node<?> node) {
		if(node.isDetached()) {
			throw new IllegalArgumentException("Unable to delete a node already detached");
		}
		Entity parentEntity = node.getParent();
		int index = node.getIndex();
		Node<?> deletedNode = parentEntity.remove(node.getName(), index);
		removeValidationCounts(deletedNode.getInternalId());
		return deletedNode;
	}
	
	public void setErrorConfirmed(Attribute<?,?> attribute, boolean confirmed){
		int fieldCount = attribute.getFieldCount();
		
		for( int i=0; i <fieldCount; i++ ){
			Field<?> field = attribute.getField(i);
			field.getState().set(CONFIRMED_ERROR_POSITION, confirmed);
		}
	}
	
	public boolean isErrorConfirmed(Attribute<?,?> attribute){
		int fieldCount = attribute.getFieldCount();		
		for( int i=0; i <fieldCount; i++ ){
			Field<?> field = attribute.getField(i);
			if( !field.getState().get(CONFIRMED_ERROR_POSITION) ){
				return false;
			}
		}
		return true;
	}
	
	public void setMissingApproved(Entity parentEntity, String childName, boolean approved) {
		org.openforis.idm.model.State childState = parentEntity.getChildState(childName);
		childState.set(APPROVED_MISSING_POSITION, approved);
	}
	
	public boolean isMissingApproved(Entity parentEntity, String childName){
		org.openforis.idm.model.State childState = parentEntity.getChildState(childName);
		return childState.get(APPROVED_MISSING_POSITION);
	} 
	
	public void setDefaultValueApplied(Attribute<?, ?> attribute, boolean applied) {
		int fieldCount = attribute.getFieldCount();
		
		for( int i=0; i <fieldCount; i++ ){
			Field<?> field = attribute.getField(i);
			field.getState().set(DEFAULT_APPLIED_POSITION, applied);
		}
	}
	
	public boolean isDefaultValueApplied(Attribute<?, ?> attribute) {
		int fieldCount = attribute.getFieldCount();		
		for( int i=0; i <fieldCount; i++ ){
			Field<?> field = attribute.getField(i);
			if( !field.getState().get(DEFAULT_APPLIED_POSITION) ){
				return false;
			}
		}
		return true;
	}

	public void updateValidationMinCounts(Integer entityId, String childName, ValidationResultFlag flag) {
		Set<String> errors = clearEntityValidationCounts(minCountErrorCounts, entityId, childName);
		Set<String> warnings = clearEntityValidationCounts(minCountWarningCounts, entityId, childName);
		switch (flag) {
			case ERROR:
				errors.add(childName);
				break;
			case WARNING:
				warnings.add(childName);
				break;
		}
		this.missing = null;
		this.missingErrors = null;
		this.missingWarnings = null;
		this.errors = null;
		this.warnings = null;
	}

	public void updateValidationMaxCounts(Integer entityId, String childName, ValidationResultFlag flag) {
		Set<String> errors = clearEntityValidationCounts(maxCountErrorCounts, entityId, childName);
		Set<String> warnings = clearEntityValidationCounts(maxCountWarningCounts, entityId, childName);
		
		switch(flag) {
		case ERROR:
			errors.add(childName);
			break;
		case WARNING:
			warnings.add(childName);
			break;
		}
		this.errors = null;
		this.warnings = null;
	}
	
	public void updateValidationCounts(Integer attributeId, ValidationResults validationResults) {
		removeValidationCounts(attributeId);
		
		int errorCounts = validationResults.getErrors().size();
		int warningCounts = validationResults.getWarnings().size();
		this.errorCounts.put(attributeId, errorCounts);
		this.warningCounts.put(attributeId, warningCounts);
		
		errors = null;
		warnings = null;
	}
	
	public void updateSkippedCount(Integer attributeId) {
		removeValidationCounts(attributeId);
		skippedNodes.add(attributeId);
		skipped = null;
	}
	
	public void removeValidationCounts(Integer nodeId) {
		Node<?> node = this.getNodeByInternalId(nodeId);
		if(node instanceof Attribute<?, ?>) {
			skippedNodes.remove(nodeId);
			errorCounts.remove(nodeId);
			warningCounts.remove(nodeId);
		} else {
			minCountErrorCounts.remove(nodeId);
			maxCountErrorCounts.remove(nodeId);
			minCountWarningCounts.remove(nodeId);
			maxCountWarningCounts.remove(nodeId);
		}
		skipped = null;
		missing = null;
		missingErrors = null;
		missingWarnings = null;
		errors = null;
		warnings = null;
	}
	
	public Step getStep() {
		return step;
	}

	public void setStep(Step step) {
		this.step = step;
	}
	
	public State getState() {
		return state;
	}
	
	public void setState(State state) {
		this.state = state;
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
		if ( skipped == null ) {
			skipped = skippedNodes.size();
		}
		return skipped;
	}

	public void setSkipped(Integer skipped) {
		this.skipped = skipped;
	}

	public Integer getMissing() {
		if (missing == null) {
			Integer errors = getMissingErrors();
			Integer warnings = getMissingWarnings();
			missing = errors + warnings;
		}
		return missing;
	}
	
	public Integer getMissingErrors() {
		if ( missingErrors == null ) {
			missingErrors = getMissingCount( minCountErrorCounts );
		}
		return missingErrors;
	}
	
	public Integer getMissingWarnings() {
		if ( missingWarnings == null ) {
			missingWarnings = getMissingCount( minCountWarningCounts);
		}
		return missingWarnings;
	}

	public void setMissing(Integer missing) {
		this.missing = missing;
	}

	public Integer getErrors() {
		if(errors == null) {
			errors = getAttributeValidationCount(errorCounts);
			errors += getEntityValidationCount(maxCountErrorCounts);
		}
		return errors;
	}

	public void setErrors(Integer errors) {
		this.errors = errors;
	}

	public Integer getWarnings() {
		if(warnings == null) {
			warnings = getAttributeValidationCount(warningCounts);
			warnings += getEntityValidationCount(maxCountWarningCounts);
		}
		return warnings;
	}

	public void setWarnings(Integer warnings) {
		this.warnings = warnings;
	}

	public List<String> getRootEntityKeyValues() {
		return rootEntityKeyValues;
	}
	
	public void updateRootEntityKeyValues(){
		Entity rootEntity = getRootEntity();
		if(rootEntity != null) {
			rootEntityKeyValues = new ArrayList<String>();
			EntityDefinition rootEntityDefn = rootEntity.getDefinition();
			List<AttributeDefinition> keyDefns = rootEntityDefn.getKeyAttributeDefinitions();
			for (AttributeDefinition keyDefn : keyDefns) {
				String keyValue = null;
				Node<?> keyNode = rootEntity.get(keyDefn.getName(), 0);
				if(keyNode instanceof CodeAttribute) {
					Code code = ((CodeAttribute) keyNode).getValue();
					if(code != null) {
						keyValue = code.getCode();
					}
				} else if(keyNode instanceof TextAttribute) {
					keyValue = ((TextAttribute) keyNode).getText();
				} else if(keyNode instanceof NumberAttribute<?,?>) {
					Number obj = ((NumberAttribute<?,?>) keyNode).getNumber();
					if(obj != null) {
						keyValue = obj.toString();
					}
				}
				if(StringUtils.isNotEmpty(keyValue)){
					rootEntityKeyValues.add(keyValue);
				} else {
					//todo throw error in this case?
					rootEntityKeyValues.add(null);
				}
			}
		}
	}

	public void updateEntityCounts() {
		Entity rootEntity = getRootEntity();
		List<EntityDefinition> countableDefns = getCountableEntitiesInList();
		
		//set counts
		List<Integer> counts = new ArrayList<Integer>();
		for (EntityDefinition defn : countableDefns) {
			String name = defn.getName();
			int count = rootEntity.getCount(name);
			counts.add(count);
		}
		entityCounts = counts;
	}
	
	public void setRootEntityKeyValues(List<String> keys) {
		this.rootEntityKeyValues = keys;
	}

	public List<Integer> getEntityCounts() {
		return entityCounts;
	}

	public void setEntityCounts(List<Integer> counts) {
		this.entityCounts = counts;
	}

	private Set<String> clearEntityValidationCounts(Map<Integer, Set<String>> counts, Integer entityId, String childName) {
		Set<String> set = counts.get(entityId);
		if(set == null) {
			set = new HashSet<String>();
			counts.put(entityId, set);
		} else {
			set.remove(childName);
		}
		return set;
	}
	
	private Integer getEntityValidationCount(Map<Integer, Set<String>> map) {
		int count = 0;
		for (Set<String> set : map.values()) {
			count += set.size();
		}
		return count;
	}
	
	private Integer getMissingCount(Map<Integer, Set<String>> minCounts) {
		int result = 0;
		Set<Integer> keySet = minCounts.keySet();
		for (Integer id : keySet) {
			Entity entity = (Entity) getNodeByInternalId(id);
			Set<String> set = minCounts.get(id);
			for (String childName : set) {
				int missingCount = entity.getMissingCount(childName);
				result += missingCount;
			}
		}
		return result;
	}
	
	private Integer getAttributeValidationCount(Map<Integer, Integer> map) {
		int count = 0;
		for (Integer i : map.values()) {
			count += i;
		}
		return count;
	}

	/**
	 * Clear all node states and all attribute symbols
	 */
	public void clearNodeStates() {
		Entity rootEntity = getRootEntity();
		rootEntity.traverse( new NodeVisitor() {
			@Override
			public void visit(Node<? extends NodeDefinition> node, int idx) {
				if ( node instanceof Attribute ) {
					Attribute<?,?> attribute = (Attribute<?, ?>) node;
//					if ( step == Step.ENTRY ) {
//						attribute.clearFieldSymbols();
//					}
					attribute.clearFieldStates();
					attribute.clearValidationResults();
				} else if( node instanceof Entity ) {
					Entity entity = (Entity) node;
					entity.clearChildStates();
					
					EntityDefinition definition = entity.getDefinition();
					List<NodeDefinition> childDefinitions = definition.getChildDefinitions();
					for (NodeDefinition childDefinition : childDefinitions) {
						String childName = childDefinition.getName();
						entity.clearRelevanceState(childName);
						entity.clearRequiredState(childName);
					}
				}
			} 
		} );
	}
	
	/**
	 * Update all derived states of all nodes
	 */
	public void updateDerivedStates() {
		initErrorCountInfo();
		Entity rootEntity = getRootEntity();
		rootEntity.traverse(new NodeVisitor() {
			@Override
			public void visit(Node<? extends NodeDefinition> node, int idx) {
				if ( node instanceof Attribute ) {
					Attribute<?,?> attribute = (Attribute<?, ?>) node;
					attribute.validateValue();
				} else if ( node instanceof Entity ) {
					Entity entity = (Entity) node;
					ModelVersion version = getVersion();
					EntityDefinition definition = entity.getDefinition();
					List<NodeDefinition> childDefinitions = definition.getChildDefinitions();
					for (NodeDefinition childDefinition : childDefinitions) {
						if ( version.isApplicable(childDefinition) ) {
							String childName = childDefinition.getName();
							entity.validateMaxCount( childName );
							entity.validateMinCount( childName );
						}
					}
				}
			}
		});
	}
	
	/**
	 * Returns first level entity definitions that have the attribute countInSummaryList set to true
	 * 
	 * @param rootEntityDefinition
	 * @return 
	 */
	private List<EntityDefinition> getCountableEntitiesInList() {
		List<EntityDefinition> result = new ArrayList<EntityDefinition>();
		EntityDefinition rootEntityDefinition = getRootEntity().getDefinition();
		List<NodeDefinition> childDefinitions = rootEntityDefinition .getChildDefinitions();
		for (NodeDefinition childDefinition : childDefinitions) {
			if(childDefinition instanceof EntityDefinition) {
				EntityDefinition entityDefinition = (EntityDefinition) childDefinition;
				String annotation = childDefinition.getAnnotation(UIOptions.Annotation.COUNT_IN_SUMMARY_LIST.getQName());
				if(annotation != null && Boolean.parseBoolean(annotation)) {
					result.add(entityDefinition);
				}
			}
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((createdBy == null) ? 0 : createdBy.hashCode());
		result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
		result = prime * result + ((modifiedBy == null) ? 0 : modifiedBy.hashCode());
		result = prime * result + ((modifiedDate == null) ? 0 : modifiedDate.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((step == null) ? 0 : step.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CollectRecord other = (CollectRecord) obj;
		if (createdBy == null) {
			if (other.createdBy != null)
				return false;
		} else if (!createdBy.equals(other.createdBy))
			return false;
		if (creationDate == null) {
			if (other.creationDate != null)
				return false;
		} else if (!creationDate.equals(other.creationDate))
			return false;
		if (modifiedBy == null) {
			if (other.modifiedBy != null)
				return false;
		} else if (!modifiedBy.equals(other.modifiedBy))
			return false;
		if (modifiedDate == null) {
			if (other.modifiedDate != null)
				return false;
		} else if (!modifiedDate.equals(other.modifiedDate))
			return false;
		if (state != other.state)
			return false;
		if (step != other.step)
			return false;
		return true;
	}
	
}
