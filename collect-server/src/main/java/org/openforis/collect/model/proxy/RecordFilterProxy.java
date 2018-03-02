package org.openforis.collect.model.proxy;

import java.util.Date;
import java.util.List;

import org.openforis.collect.Proxy;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;

public class RecordFilterProxy implements Proxy {

	private boolean caseSensitiveKeyValues;
	private Integer offset;
	private List<String> keyValues;
	private Integer maxNumberOfRecords;
	private Date modifiedSince;
	private List<Integer> ownerIds;
	private Integer recordId;
	private Integer rootEntityId;
	private Step step;
	private Step stepGreaterOrEqual;
	private int surveyId;

	public RecordFilterProxy(RecordFilter filter) {
		super();
		this.caseSensitiveKeyValues = filter.isCaseSensitiveKeyValues();
		this.keyValues = filter.getKeyValues();
		this.maxNumberOfRecords = filter.getMaxNumberOfRecords();
		this.modifiedSince = filter.getModifiedSince();
		this.offset = filter.getOffset();
		this.ownerIds = filter.getOwnerIds();
		this.recordId = filter.getRecordId();
		this.rootEntityId = filter.getRootEntityId();
		this.step = filter.getStep();
		this.stepGreaterOrEqual = filter.getStepGreaterOrEqual();
		this.surveyId = filter.getSurveyId();
	}
	
	public RecordFilter toFilter(CollectSurvey survey) {
		RecordFilter filter = new RecordFilter(survey);
		filter.setCaseSensitiveKeyValues(isCaseSensitiveKeyValues());
		filter.setKeyValues(getKeyValues());
		filter.setMaxNumberOfRecords(getMaxNumberOfRecords());
		filter.setModifiedSince(getModifiedSince());
		filter.setOffset(getOffset());
		filter.setOwnerIds(getOwnerIds());
		filter.setRecordId(getRecordId());
		filter.setRootEntityId(getRootEntityId());
		filter.setStep(getStep());
		filter.setStepGreaterOrEqual(getStepGreaterOrEqual());
		return filter;
	}

	public boolean isCaseSensitiveKeyValues() {
		return caseSensitiveKeyValues;
	}

	public void setCaseSensitiveKeyValues(boolean caseSensitiveKeyValues) {
		this.caseSensitiveKeyValues = caseSensitiveKeyValues;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public List<String> getKeyValues() {
		return keyValues;
	}

	public void setKeyValues(List<String> keyValues) {
		this.keyValues = keyValues;
	}

	public Integer getMaxNumberOfRecords() {
		return maxNumberOfRecords;
	}

	public void setMaxNumberOfRecords(Integer maxNumberOfRecords) {
		this.maxNumberOfRecords = maxNumberOfRecords;
	}

	public Date getModifiedSince() {
		return modifiedSince;
	}

	public void setModifiedSince(Date modifiedSince) {
		this.modifiedSince = modifiedSince;
	}

	public List<Integer> getOwnerIds() {
		return ownerIds;
	}
	
	public void setOwnerIds(List<Integer> ownerIds) {
		this.ownerIds = ownerIds;
	}

	public Integer getRecordId() {
		return recordId;
	}

	public void setRecordId(Integer recordId) {
		this.recordId = recordId;
	}

	public Integer getRootEntityId() {
		return rootEntityId;
	}

	public void setRootEntityId(Integer rootEntityId) {
		this.rootEntityId = rootEntityId;
	}

	public Step getStep() {
		return step;
	}

	public void setStep(Step step) {
		this.step = step;
	}

	public Step getStepGreaterOrEqual() {
		return stepGreaterOrEqual;
	}

	public void setStepGreaterOrEqual(Step stepGreaterOrEqual) {
		this.stepGreaterOrEqual = stepGreaterOrEqual;
	}

	public int getSurveyId() {
		return surveyId;
	}

	public void setSurveyId(int surveyId) {
		this.surveyId = surveyId;
	}
	
}
