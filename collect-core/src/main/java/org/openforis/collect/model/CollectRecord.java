package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openforis.collect.Collect;
import org.openforis.collect.event.RecordStep;
import org.openforis.collect.utils.Numbers;
import org.openforis.commons.versioning.Version;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.FileAttribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodeVisitor;
import org.openforis.idm.model.Record;

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
		ENTRY		(1, RecordStep.ENTRY), 
		CLEANSING	(2, RecordStep.CLEANSING), 
		ANALYSIS	(3, RecordStep.ANALYSIS);

		private int stepNumber;
		private RecordStep recordStep;

		private Step(int stepNumber, RecordStep recordStep) {
			this.stepNumber = stepNumber;
			this.recordStep = recordStep;
		}

		public int getStepNumber() {
			return stepNumber;
		}

		public RecordStep toRecordStep() {
			return recordStep;
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
		
		public static Step fromRecordStep(RecordStep recordStep) {
			Step[] values = Step.values();
			for (Step step : values) {
				if (step.recordStep == recordStep) {
					return step;
				}
			}
			throw new IllegalArgumentException("Unknown record step: " + recordStep);
		}
		
		public boolean hasNext() {
			switch(this) {
			case ENTRY:
			case CLEANSING:
				return true;
			default:
				return false;
			}
		}
		
		public Step getNext() {
			switch(this) {
				case ENTRY:
					return CLEANSING;
				case CLEANSING:
					return ANALYSIS;
				default:
					throw new IllegalStateException(String.format(
							"The step %s is the last step of the workflow and does not have a next one.", this.name()));
			}
		}
		
		public boolean hasPrevious() {
			switch(this) {
			case CLEANSING:
			case ANALYSIS:
				return true;
			default:
				return false;
			}
		}
		
		public Step getPrevious() {
			switch(this) {
				case CLEANSING:
					return Step.ENTRY;
				case ANALYSIS:
					return Step.CLEANSING;
				default:
					throw new IllegalStateException(String.format(
							"The step %s is the first of the workflow and does not have a previous one.", this.name()));
			}
		}
		
		public List<Step> getPreviousSteps() {
			List<Step> result = new ArrayList<Step>();
			for (Step s : Step.values()) {
				if (s.beforeEqual(this)) {
					result.add(s);
				}
			}
			return result;
		}
		
		public boolean before(Step step) {
			return this.compareTo(step) < 0;
		}
		
		public boolean beforeEqual(Step step) {
			return this.compareTo(step) <= 0;
		}
		
		
		public boolean after(Step step) {
			return this.compareTo(step) > 0;
		}

		public boolean afterEqual(Step step) {
			return this.compareTo(step) >= 0;
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
	
	private transient Version applicationVersion;
	private transient Step step;
	private transient Step dataStep;
	private transient State state;
	private transient Integer workflowSequenceNumber;
	private transient Integer dataWorkflowSequenceNumber;

	private transient Date creationDate;
	private transient User createdBy;
	private transient Date modifiedDate;
	private transient User modifiedBy;
	private transient Date dataCreationDate;
	private transient User dataCreatedBy;
	private transient Date dataModifiedDate;
	private transient User dataModifiedBy;
	private transient List<String> dataRootEntityKeyValues;
	private transient List<Integer> dataEntityCounts;
	private transient List<String> dataQualifierValues;
	private transient List<String> dataSummaryValues;
	private transient User owner;
	private transient Integer missing;
	private transient Integer missingErrors;
	private transient Integer missingWarnings;
	private transient Integer skipped;
	private transient Integer errors;
	private transient Integer warnings;
	
	private transient List<String> rootEntityKeyValues;
	private transient List<Integer> entityCounts;
	private transient List<String> qualifierValues;
	private transient List<String> summaryValues;
	
	private RecordValidationCache validationCache;

	public CollectRecord(CollectSurvey survey, String versionName, String rootEntityName) {
		this(survey, versionName, rootEntityName, true);
	}
	
	public CollectRecord(CollectSurvey survey, String versionName, String rootEntityName, boolean dependencyGraphsEnabled) {
		this(survey, versionName, rootEntityName, dependencyGraphsEnabled, false, true);
	}
	
	public CollectRecord(CollectSurvey survey, String versionName, String rootEntityName, 
			boolean dependencyGraphsEnabled, boolean enableValidationDependencyGraphs, boolean ignoreExistingRecordValidationErrors) {
		this(survey, versionName, survey.getSchema().getRootEntityDefinition(rootEntityName), dependencyGraphsEnabled, enableValidationDependencyGraphs, ignoreExistingRecordValidationErrors);
	}

	public CollectRecord(CollectSurvey survey, String versionName, String rootEntityName, boolean enableValidationDependencyGraphs, boolean ignoreExistingRecordValidationErrors) {
		this(survey, versionName, survey.getSchema().getRootEntityDefinition(rootEntityName), true, enableValidationDependencyGraphs, ignoreExistingRecordValidationErrors);
	}
	
	public CollectRecord(CollectSurvey survey, String versionName, EntityDefinition rootEntityDefinition, boolean enableValidationDependencyGraphs) {
		this(survey, versionName, rootEntityDefinition, true, enableValidationDependencyGraphs, false);
	}
	
	public CollectRecord(CollectSurvey survey, String versionName, EntityDefinition rootEntityDefinition, 
			boolean dependencyGraphsEnabled, boolean enableValidationDependencyGraphs, boolean ignoreExistingRecordValidationErrors) {
		super(survey, versionName, rootEntityDefinition, dependencyGraphsEnabled, enableValidationDependencyGraphs, ignoreExistingRecordValidationErrors);
		this.applicationVersion = Collect.VERSION;
		this.step = Step.ENTRY;
		this.dataStep = Step.ENTRY;
		// use List to preserve the order of the keys and counts
		this.rootEntityKeyValues = new ArrayList<String>();
		this.entityCounts = new ArrayList<Integer>();
		this.qualifierValues = new ArrayList<String>();
		this.dataSummaryValues = new ArrayList<String>();
	}
	
	@Override
	public void replaceRootEntity(Entity rootEntity) {
		super.replaceRootEntity(rootEntity);
		this.updateSummaryFields();
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
	
	public Version getApplicationVersion() {
		return applicationVersion;
	}
	
	public void setApplicationVersion(Version applicationVersion) {
		this.applicationVersion = applicationVersion;
	}
	
	public Step getStep() {
		return step;
	}

	public void setStep(Step step) {
		this.step = step;
	}
	
	public Step getDataStep() {
		return dataStep;
	}
	
	public void setDataStep(Step dataStep) {
		this.dataStep = dataStep;
	}
	
	public State getState() {
		return state;
	}
	
	public void setState(State state) {
		this.state = state;
	}
	
	public Integer getWorkflowSequenceNumber() {
		return workflowSequenceNumber;
	}
	
	public void setWorkflowSequenceNumber(Integer workflowSequenceNumber) {
		this.workflowSequenceNumber = workflowSequenceNumber;
	}
	
	public Integer getDataWorkflowSequenceNumber() {
		return dataWorkflowSequenceNumber;
	}
	
	public void setDataWorkflowSequenceNumber(Integer dataWorkflowSequenceNumber) {
		this.dataWorkflowSequenceNumber = dataWorkflowSequenceNumber;
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
	
	public User getDataCreatedBy() {
		return dataCreatedBy;
	}
	
	public void setDataCreatedBy(User dataCreatedBy) {
		this.dataCreatedBy = dataCreatedBy;
	}
	
	public Date getDataCreationDate() {
		return dataCreationDate;
	}
	
	public void setDataCreationDate(Date dataCreationDate) {
		this.dataCreationDate = dataCreationDate;
	}
	
	public User getDataModifiedBy() {
		return dataModifiedBy;
	}
	
	public void setDataModifiedBy(User dataModifiedBy) {
		this.dataModifiedBy = dataModifiedBy;
	}
	
	public Date getDataModifiedDate() {
		return dataModifiedDate;
	}
	
	public void setDataModifiedDate(Date dataModifiedDate) {
		this.dataModifiedDate = dataModifiedDate;
	}
	
	public List<Integer> getDataEntityCounts() {
		return dataEntityCounts;
	}
	
	public void setDataEntityCounts(List<Integer> dataEntityCounts) {
		this.dataEntityCounts = dataEntityCounts;
	}
	
	public List<String> getDataQualifierValues() {
		return dataQualifierValues;
	}
	
	public void setDataQualifierValues(List<String> dataQualifierValues) {
		this.dataQualifierValues = dataQualifierValues;
	}
	
	public List<String> getDataRootEntityKeyValues() {
		return dataRootEntityKeyValues;
	}
	
	public void setDataRootEntityKeyValues(List<String> dataRootEntityKeyValues) {
		this.dataRootEntityKeyValues = dataRootEntityKeyValues;
	}
	
	public List<String> getDataSummaryValues() {
		return dataSummaryValues;
	}

	public void setDataSummaryValues(List<String> summaryValues) {
		this.dataSummaryValues = summaryValues;
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

	public Integer getTotalErrors() {
		switch(step) {
		case ENTRY:
			return Numbers.sum(getErrors(), getSkipped());
		default:
			return Numbers.sum(getErrors(), getMissingErrors());
		}
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
	
	public void setRootEntityKeyValues(List<String> keys) {
		this.rootEntityKeyValues = keys;
	}
	
	public List<String> getSummaryValues() {
		return summaryValues;
	}
	
	public void setSummaryValues(List<String> summaryValues) {
		this.summaryValues = summaryValues;
	}

	public List<String> getQualifierValues() {
		return qualifierValues;
	}
	
	public void setQualifierValues(List<String> qualifierValues) {
		this.qualifierValues = qualifierValues;
	}
	
	public RecordValidationCache getValidationCache() {
		return validationCache;
	}
	
	public boolean isErrorConfirmed(Attribute<?,?> attribute){
		for (Field<?> field : attribute.getFields()) {
			if (!field.getState().get(CONFIRMED_ERROR_POSITION)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isMissingApproved(Entity parentEntity, NodeDefinition childDef){
		org.openforis.idm.model.State childState = parentEntity.getChildState(childDef);
		return childState.get(APPROVED_MISSING_POSITION);
	} 
	
	public boolean isDefaultValueApplied(Attribute<?, ?> attribute) {
		for (Field<?> field : attribute.getFields()) {
			if (!field.getState().get(DEFAULT_APPLIED_POSITION)) {
				return false;
			}
		}
		return true;
	}
	
	public void updateSummaryFields() {
		updateRootEntityKeyValues();
		updateEntityCounts();
		updateDataSummaryFields();
		updateQualifiers();
	}
	
	private void updateRootEntityKeyValues(){
		Entity rootEntity = getRootEntity();
		if(rootEntity != null) {
			List<AttributeDefinition> keyAttributeDefinitions = rootEntity.getDefinition().getKeyAttributeDefinitions();
			rootEntityKeyValues = extractValues(keyAttributeDefinitions);
		}
	}

	private void updateEntityCounts() {
		List<Integer> counts = new ArrayList<Integer>();
		List<EntityDefinition> countableDefns = getSurvey().getSchema().getCountableEntitiesInRecordList(getRootEntity().getDefinition());
		for (EntityDefinition defn : countableDefns) {
			List<Node<?>> nodes = findNodesByPath(defn.getPath());
			counts.add(nodes.size());
		}
		this.entityCounts = counts;
	}
	
	private void updateDataSummaryFields() {
		List<AttributeDefinition> attrDefs = getSurvey().getSchema().getSummaryAttributeDefinitions(getRootEntity().getDefinition());
		this.dataSummaryValues = extractValues(attrDefs);
	}
	
	private void updateQualifiers() {
		List<AttributeDefinition> attrDefs = getSurvey().getSchema().getQualifierAttributeDefinitions(getRootEntity().getDefinition());
		this.qualifierValues = extractValues(attrDefs);
	}
	
	private List<String> extractValues(List<AttributeDefinition> attrDefs) {
		List<String> values = new ArrayList<String>();
		for (AttributeDefinition keyDefn : attrDefs) {
			Attribute<?, ?> keyNode = this.findNodeByPath(keyDefn.getPath());
			if ( keyNode == null || keyNode.isEmpty() ) {
				//TODO throw error in this case?
				values.add(null);
			} else {
				if (! keyNode.isEmpty()) {
					String keyValue = keyNode.extractTextValue();
					values.add(keyValue);
				}
			}
		}
		return values;
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
	
	public void updateMinCountsValidationCache(Entity entity, NodeDefinition childDef, ValidationResultFlag flag) {
		validationCache.updateMinCountInfo(entity.getInternalId(), childDef, flag);
		this.missing = null;
		this.missingErrors = null;
		this.missingWarnings = null;
		this.errors = null;
		this.warnings = null;
	}

	public void updateMaxCountsValidationCache(Entity entity, NodeDefinition childDef, ValidationResultFlag flag) {
		validationCache.updateMaxCountInfo(entity.getInternalId(), childDef, flag);
		this.errors = null;
		this.warnings = null;
	}
	
	public void updateAttributeValidationCache(Attribute<?, ?> attribute, ValidationResults validationResults) {
		Integer attributeId = attribute.getInternalId();
		
		removeValidationCache(attribute);
		
		validationCache.setAttributeErrorCount(attributeId, validationResults.countErrors());
		validationCache.setAttributeWarningCount(attributeId, validationResults.countWarnings());
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
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (!super.deepEquals(obj))
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
