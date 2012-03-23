package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
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
	private transient User lockedBy;
	private transient Integer missing;
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
		minCountErrorCounts = new HashMap<Integer, Set<String>>();
		minCountWarningCounts = new HashMap<Integer, Set<String>>();
		maxCountErrorCounts = new HashMap<Integer, Set<String>>();
		maxCountWarningCounts = new HashMap<Integer, Set<String>>();
		errorCounts = new HashMap<Integer, Integer>();
		warningCounts = new HashMap<Integer, Integer>();
		skippedNodes = new HashSet<Integer>();
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
		if ( missing == null ){
			missing = getEntityValidationCount( minCountErrorCounts );
			missing += getEntityValidationCount( minCountWarningCounts );
		}
		return missing;
	}

	public void setMissing(Integer missing) {
		this.missing = missing;
	}

	public Integer getErrors() {
		if(errors == null) {
			errors = getEntityValidationCount(minCountErrorCounts);
			errors += getEntityValidationCount(maxCountErrorCounts);
			errors += getAttributeValidationCount(errorCounts);
		}
		return errors;
	}

	public void setErrors(Integer errors) {
		this.errors = errors;
	}

	public Integer getWarnings() {
		if(warnings == null) {
			warnings = getEntityValidationCount(minCountWarningCounts);
			warnings += getEntityValidationCount(maxCountWarningCounts);
			warnings += getAttributeValidationCount(warningCounts);
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
			String keyValue = null;
			for (AttributeDefinition keyDefn : keyDefns) {
				Node<?> keyNode = rootEntity.get(keyDefn.getName(), 0);
				if(keyNode instanceof CodeAttribute) {
					Code code = ((CodeAttribute) keyNode).getValue();
					if(code != null) {
						keyValue = code.getCode();
					}
				} else if(keyNode instanceof TextAttribute) {
					keyValue = ((TextAttribute) keyNode).getValue();
				} else if(keyNode instanceof NumberAttribute<?>) {
					Object obj = ((NumberAttribute<?>) keyNode).getValue();
					if(obj != null) {
						keyValue = obj.toString();
					}
				}
				if(StringUtils.isNotEmpty(keyValue)){
					rootEntityKeyValues.add(keyValue);
				}
			}
		}
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

	public User getLockedBy() {
		return lockedBy;
	}
	
	public void setLockedBy(User lockedBy) {
		this.lockedBy = lockedBy;
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
	
	private Integer getAttributeValidationCount(Map<Integer, Integer> map) {
		int count = 0;
		for (Integer i : map.values()) {
			count += i;
		}
		return count;
	}
	
}
