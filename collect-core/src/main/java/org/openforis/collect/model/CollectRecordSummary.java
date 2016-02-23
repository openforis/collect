package org.openforis.collect.model;

import java.util.Date;
import java.util.List;

import org.openforis.collect.model.CollectRecord.State;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.idm.metamodel.ModelVersion;

public class CollectRecordSummary {

	private int completionPercent;
	private User createdBy;
	private Date creationDate;
	private List<Integer> entityCounts;
	private Integer errors;
	private int filledAttributesCount;
	private Integer id;
	private Integer missing;
	private Integer missingErrors;
	private Integer missingWarnings;
	private User modifiedBy;
	private Date modifiedDate;
	private List<String> rootEntityKeyValues;
	private Integer skipped;
	private State state;
	private Step step;
	private CollectSurvey survey;
	private Integer totalErrors;

	private ModelVersion version;
	private User owner;

	public static CollectRecordSummary fromRecord(CollectRecord record) {
		CollectSurvey survey = (CollectSurvey) record.getSurvey();
		ModelVersion version = record.getVersion();
		CollectRecordSummary result = new CollectRecordSummary();
		result.setCompletionPercent(record.calculateCompletionPercent());
		result.setCreatedBy(record.getCreatedBy());
		result.setCreationDate(record.getCreationDate());
		result.setEntityCounts(record.getEntityCounts());
		result.setTotalErrors(record.getTotalErrors());
		result.setErrors(record.getErrors());
		result.setFilledAttributesCount(record.countTotalFilledAttributes());
		result.setId(record.getId());
		result.setMissing(record.getMissing());
		result.setMissingErrors(record.getMissingErrors());
		result.setMissingWarnings(record.getMissingWarnings());
		result.setModifiedBy(record.getModifiedBy());
		result.setModifiedDate(record.getModifiedDate());
		result.setRootEntityKeyValues(record.getRootEntityKeyValues());
		result.setSkipped(record.getSkipped());
		result.setState(record.getState());
		result.setStep(record.getStep());
		result.setSurvey(survey);
		result.setVersion(version);
		result.setOwner(record.getOwner());
		return result;
	}

	public int getCompletionPercent() {
		return completionPercent;
	}
	
	public void setCompletionPercent(int completionPercent) {
		this.completionPercent = completionPercent;
	}
	
	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public List<Integer> getEntityCounts() {
		return entityCounts;
	}

	public void setEntityCounts(List<Integer> entityCounts) {
		this.entityCounts = entityCounts;
	}

	public Integer getErrors() {
		return errors;
	}

	public void setErrors(Integer errors) {
		this.errors = errors;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getMissing() {
		return missing;
	}

	public void setMissing(Integer missing) {
		this.missing = missing;
	}

	public Integer getMissingErrors() {
		return missingErrors;
	}
	
	public void setMissingErrors(Integer missingErrors) {
		this.missingErrors = missingErrors;
	}
	
	public Integer getMissingWarnings() {
		return missingWarnings;
	}
	
	public void setMissingWarnings(Integer missingWarnings) {
		this.missingWarnings = missingWarnings;
	}
	
	public User getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(User modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public List<String> getRootEntityKeyValues() {
		return rootEntityKeyValues;
	}

	public void setRootEntityKeyValues(List<String> rootEntityKeyValues) {
		this.rootEntityKeyValues = rootEntityKeyValues;
	}

	public Integer getSkipped() {
		return skipped;
	}

	public void setSkipped(Integer skipped) {
		this.skipped = skipped;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Step getStep() {
		return step;
	}

	public void setStep(Step step) {
		this.step = step;
	}

	public CollectSurvey getSurvey() {
		return survey;
	}

	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}

	public ModelVersion getVersion() {
		return version;
	}

	public void setVersion(ModelVersion version) {
		this.version = version;
	}
	
	public User getOwner() {
		return owner;
	}
	
	public void setOwner(User owner) {
		this.owner = owner;
	}

	public int getFilledAttributesCount() {
		return filledAttributesCount;
	}

	public void setFilledAttributesCount(int filledAttributesCount) {
		this.filledAttributesCount = filledAttributesCount;
	}

	public Integer getTotalErrors() {
		return totalErrors;
	}

	public void setTotalErrors(Integer totalErrors) {
		this.totalErrors = totalErrors;
	}
}
