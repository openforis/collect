package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openforis.collect.metamodel.CollectAnnotations.Annotation;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.FileAttribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodeVisitor;
import org.openforis.idm.model.NumberAttribute;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.TextAttribute;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class CollectRecord extends Record {

	public static final int APPROVED_MISSING_POSITION = 0;
	public static final int CONFIRMED_ERROR_POSITION = 0;
	public static final int DEFAULT_APPLIED_POSITION = 1;
	
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
	private transient User owner;
	private transient Integer missing;
	private transient Integer missingErrors;
	private transient Integer missingWarnings;
	private transient Integer skipped;
	private transient Integer errors;
	private transient Integer warnings;
	
	private List<String> rootEntityKeyValues;
	private List<Integer> entityCounts;
	
	private RecordValidationCache validationCache;

	public CollectRecord(CollectSurvey survey, String versionName) {
		super(survey, versionName);
		this.step = Step.ENTRY;
		// use List to preserve the order of the keys and counts
		this.rootEntityKeyValues = new ArrayList<String>();
		this.entityCounts = new ArrayList<Integer>();
	}
	
	@Override
	public void replaceRootEntity(Entity rootEntity) {
		super.replaceRootEntity(rootEntity);
		updateRootEntityKeyValues();
		updateEntityCounts();
	}
	
	@Override
	protected void resetValidationDependencies() {
		super.resetValidationDependencies();
		resetValidationInfo();
	}
	
	protected void resetValidationInfo() {
		validationCache = new RecordValidationCache(this);
		skipped = null;
		missing = null;
		missingErrors = null;
		missingWarnings = null;
		errors = null;
		warnings = null;
	}
	
	@Override
	protected void remove(Node<?> node) {
		super.remove(node);
		removeValidationCache(node);
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
	
	public User getOwner() {
		return owner;
	}
	
	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Integer getSkipped() {
		if ( skipped == null ) {
			skipped = validationCache.getSkippedNodeIds().size();
		}
		return skipped;
	}

	public void setSkipped(Integer skipped) {
		this.skipped = skipped;
	}

	public Integer getMissing() {
		if (missing == null) {
			missing = getMissingErrors();
		}
		return missing;
	}
	
	public Integer getMissingErrors() {
		if ( missingErrors == null ) {
			missingErrors = validationCache.getTotalMissingMinCountErrors();
		}
		return missingErrors;
	}
	
	public Integer getMissingWarnings() {
		if ( missingWarnings == null ) {
			missingWarnings = validationCache.getTotalMissingMinCountWarnings();
		}
		return missingWarnings;
	}

	public void setMissing(Integer missing) {
		this.missing = missing;
	}

	public Integer getErrors() {
		if(errors == null) {
			errors = validationCache.getTotalAttributeErrors() +
					validationCache.getTotalMaxCountErrors();
		}
		return errors;
	}

	public void setErrors(Integer errors) {
		this.errors = errors;
	}

	public Integer getWarnings() {
		if(warnings == null) {
			warnings = validationCache.getTotalAttributeWarnings() +
						validationCache.getTotalMinCountWarnings() +
						validationCache.getTotalMaxCountWarnings();
		}
		return warnings;
	}

	public void setWarnings(Integer warnings) {
		this.warnings = warnings;
	}

	public List<String> getRootEntityKeyValues() {
		return rootEntityKeyValues;
	}

	public RecordValidationCache getValidationCache() {
		return validationCache;
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
	
	public boolean isMissingApproved(Entity parentEntity, String childName){
		org.openforis.idm.model.State childState = parentEntity.getChildState(childName);
		return childState.get(APPROVED_MISSING_POSITION);
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
	
	public void updateSummaryFields() {
		updateRootEntityKeyValues();
		updateEntityCounts();
	}
	
	private void updateRootEntityKeyValues(){
		Entity rootEntity = getRootEntity();
		if(rootEntity != null) {
			List<String> values = new ArrayList<String>();
			List<AttributeDefinition> keyAttributeDefinitions = rootEntity.getDefinition().getKeyAttributeDefinitions();
			for (AttributeDefinition keyDefn : keyAttributeDefinitions) {
				Node<?> keyNode = this.findNodeByPath(keyDefn.getPath());
				if ( keyNode == null || keyNode.isEmpty() ) {
					//TODO throw error in this case?
					values.add(null);
				} else {
					String keyValue = getTextValue(keyNode);
					values.add(keyValue);
				}
			}
			rootEntityKeyValues = values;
		}
	}

	private void updateEntityCounts() {
		List<Integer> counts = new ArrayList<Integer>();
		List<EntityDefinition> countableDefns = getCountableEntitiesInList();
		for (EntityDefinition defn : countableDefns) {
			List<Node<?>> nodes = findNodesByPath(defn.getPath());
			counts.add(nodes.size());
		}
		this.entityCounts = counts;
	}
	
	private String getTextValue(Node<?> keyNode) {
		if(keyNode instanceof CodeAttribute) {
			Code code = ((CodeAttribute) keyNode).getValue();
			return code == null ? null: code.getCode();
		} else if(keyNode instanceof TextAttribute) {
			return ((TextAttribute) keyNode).getText();
		} else if(keyNode instanceof NumberAttribute<?,?>) {
			Number number = ((NumberAttribute<?,?>) keyNode).getNumber();
			return number == null ? null: number.toString();
		} else {
			throw new UnsupportedOperationException("Unsopported node type: " + keyNode.getClass().getName());
		}
	}

	/**
	 * Returns first level entity definitions that have the attribute countInSummaryList set to true
	 * 
	 * @param rootEntityDefinition
	 * @return 
	 */
	protected List<EntityDefinition> getCountableEntitiesInList() {
		final List<EntityDefinition> result = new ArrayList<EntityDefinition>();
		EntityDefinition rootEntityDefinition = getRootEntity().getDefinition();
		rootEntityDefinition.traverse(new NodeDefinitionVisitor() {
			public void visit(NodeDefinition def) {
				if(def instanceof EntityDefinition) {
					String annotation = def.getAnnotation(Annotation.COUNT_IN_SUMMARY_LIST.getQName());
					if(Boolean.parseBoolean(annotation)) {
						result.add((EntityDefinition) def);
					}
				}
			}
		});
		return result;
	}
	
	public List<FileAttribute> getFileAttributes() {
		final List<FileAttribute> result = new ArrayList<FileAttribute>();
		Entity rootEntity = getRootEntity();
		rootEntity.traverse(new NodeVisitor() {
			@Override
			public void visit(Node<? extends NodeDefinition> node, int pos) {
				if ( node instanceof FileAttribute ) {
					result.add((FileAttribute) node);
				}
			}
		});
		return result;
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

	public void updateSkippedCount(Integer attributeId) {
		removeValidationCache(attributeId);
		validationCache.addSkippedNodeId(attributeId);
		skipped = null;
	}
	
	public void updateMinCountsValidationCache(Entity entity, String childName, ValidationResultFlag flag) {
		validationCache.updateMinCountInfo(entity.getInternalId(), childName, flag);
		this.missing = null;
		this.missingErrors = null;
		this.missingWarnings = null;
		this.errors = null;
		this.warnings = null;
	}

	public void updateMaxCountsValidationCache(Entity entity, String childName, ValidationResultFlag flag) {
		validationCache.updateMaxCountInfo(entity.getInternalId(), childName, flag);
		this.errors = null;
		this.warnings = null;
	}
	
	public void updateAttributeValidationCache(Attribute<?, ?> attribute, ValidationResults validationResults) {
		Integer attributeId = attribute.getInternalId();
		
		removeValidationCache(attributeId);
		
		int errorCounts = validationResults.getErrors().size();
		int warningCounts = validationResults.getWarnings().size();
		validationCache.setAttributeErrorCount(attributeId, errorCounts);
		validationCache.setAttributeWarningCount(attributeId, warningCounts);
		validationCache.setAttributeValidationResults(attributeId, validationResults);
		
		errors = null;
		warnings = null;
	}

	protected void removeValidationCache(int nodeId) {
		Node<?> node = this.getNodeByInternalId(nodeId);
		removeValidationCache(node);
	}
	
	
	private void removeValidationCache(Node<?> node) {
		validationCache.remove(node);
		skipped = null;
		missing = null;
		missingErrors = null;
		missingWarnings = null;
		errors = null;
		warnings = null;
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
