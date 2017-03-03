package org.openforis.collect.model;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.idm.metamodel.EntityDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class RecordFilter {

	private Integer offset;
	private Integer maxNumberOfRecords;
	private CollectSurvey survey;
	private int surveyId;
	private Integer rootEntityId;
	private Integer recordId;
	private Step step;
	private Step stepGreaterOrEqual;
	private Date modifiedSince;
	private Integer ownerId;
	private List<String> keyValues;
	private boolean caseSensitiveKeyValues;
	private boolean includeNullConditionsForKeyValues = false;
	
	public RecordFilter(int surveyId, Integer rootEntityId) {
		this.surveyId = surveyId;
		this.rootEntityId = rootEntityId;
	}
	
	public RecordFilter(CollectSurvey survey) {
		this(survey, (Integer) null);
	}
	
	public RecordFilter(CollectSurvey survey, Integer rootEntityId) {
		this(survey.getId(), rootEntityId);
		this.survey = survey;
		this.caseSensitiveKeyValues = true;
	}
	
	public RecordFilter(CollectSurvey survey, String rootEntityName) {
		this(survey, getRootEntityId(survey, rootEntityName));
	}

	private static Integer getRootEntityId(CollectSurvey survey,
			String rootEntityName) {
		if (rootEntityName == null) {
			return null;
		} else {
			EntityDefinition rootEntityDef = survey.getSchema().getRootEntityDefinition(rootEntityName);
			return rootEntityDef.getId();
		}
	}

	public Integer getOffset() {
		return offset;
	}
	
	public void setOffset(Integer offset) {
		this.offset = offset;
	}
	
	public Integer getMaxNumberOfRecords() {
		return maxNumberOfRecords;
	}
	
	public void setMaxNumberOfRecords(Integer maxNumberOfRecords) {
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
	
	public void setRootEntityId(Integer rootEntityId) {
		this.rootEntityId = rootEntityId;
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
		} else {
			this.keyValues = null;
		}
	}
	
	public boolean isCaseSensitiveKeyValues() {
		return caseSensitiveKeyValues;
	}

	public void setCaseSensitiveKeyValues(boolean caseSensitiveKeyValues) {
		this.caseSensitiveKeyValues = caseSensitiveKeyValues;
	}
	
	public boolean isIncludeNullConditionsForKeyValues() {
		return includeNullConditionsForKeyValues;
	}
	
	public void setIncludeNullConditionsForKeyValues(boolean includeNullConditionsForKeyValues) {
		this.includeNullConditionsForKeyValues = includeNullConditionsForKeyValues;
	}
	
}
