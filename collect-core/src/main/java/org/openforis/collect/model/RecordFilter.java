package org.openforis.collect.model;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.openforis.collect.model.CollectRecord.Step;

/**
 * 
 * @author S. Ricci
 *
 */
public class RecordFilter {

	private int offset;
	private int maxNumberOfRecords;
	private CollectSurvey survey;
	private int surveyId;
	private Integer rootEntityId;
	private Integer recordId;
	private Step step;
	private Step stepGreaterOrEqual;
	private Date modifiedSince;
	private Integer ownerId;
	private List<String> keyValues;
	
	public RecordFilter(int surveyId, Integer rootEntityId) {
		this.surveyId = surveyId;
		this.rootEntityId = rootEntityId;
		this.offset = 0;
		this.maxNumberOfRecords = Integer.MAX_VALUE;
	}
	
	public RecordFilter(CollectSurvey survey) {
		this(survey, (Integer) null);
	}
	
	public RecordFilter(CollectSurvey survey, Integer rootEntityId) {
		this(survey.getId(), rootEntityId);
		this.survey = survey;
	}

	public int getOffset() {
		return offset;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	public int getMaxNumberOfRecords() {
		return maxNumberOfRecords;
	}
	
	public void setMaxNumberOfRecords(int maxNumberOfRecords) {
		this.maxNumberOfRecords = maxNumberOfRecords;
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}
	
	public int getSurveyId() {
		return surveyId;
	}
	
	public Integer getRootEntityId() {
		return rootEntityId;
	}
	
	public Integer getRecordId() {
		return recordId;
	}
	
	public void setRecordId(Integer recordId) {
		this.recordId = recordId;
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
	
	public Date getModifiedSince() {
		return modifiedSince;
	}
	
	public void setModifiedSince(Date modifiedSince) {
		this.modifiedSince = modifiedSince;
	}
	
	public Integer getOwnerId() {
		return ownerId;
	}
	
	public void setOwnerId(Integer ownerId) {
		this.ownerId = ownerId;
	}
	
	public List<String> getKeyValues() {
		return keyValues;
	}
	
	public void setKeyValues(List<String> keyValues) {
		this.keyValues = keyValues;
	}
	
	public void setKeyValues(String[] keyValues) {
		if ( keyValues != null && keyValues.length > 0 ) {
			setKeyValues(Arrays.asList(keyValues));
		}
	}
	
}
