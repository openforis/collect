package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.idm.metamodel.AttributeDefinition;
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
	private Set<Integer> recordIds;
	private Step step;
	private Step stepGreaterOrEqual;
	private Date modifiedSince;
	private Date modifiedUntil;
	private String filterExpression;
	private List<String> keyValues;
	private List<String> qualifiers;
	private List<String> summaryValues;
	private List<Integer> ownerIds;
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
	
	public void setQualifiersByName(Map<String, String> qualifiers) {
		List<String> qualifierValues = new ArrayList<String>();
		EntityDefinition rootEntityDefinition = survey.getSchema().getRootEntityDefinition(rootEntityId);
		List<AttributeDefinition> qualifierAttrDefs = survey.getSchema().getQualifierAttributeDefinitions(rootEntityDefinition);
		for (AttributeDefinition qualifierDef : qualifierAttrDefs) {
			String qualifierVal = qualifiers.get(qualifierDef.getName());
			qualifierValues.add(qualifierVal);
		}
		setQualifiers(qualifierValues);
	}
	
	public void setOwnerId(int ownerId) {
		this.ownerIds = Arrays.asList(ownerId);
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
	
	public Date getModifiedUntil() {
		return modifiedUntil;
	}
	
	public void setModifiedUntil(Date modifiedUntil) {
		this.modifiedUntil = modifiedUntil;
	}
	
	public String getFilterExpression() {
		return filterExpression;
	}
	
	public void setFilterExpression(String filterExpression) {
		this.filterExpression = filterExpression;
	}
	
	public List<Integer> getOwnerIds() {
		return ownerIds;
	}
	
	public void setOwnerIds(List<Integer> ownerIds) {
		this.ownerIds = ownerIds;
	}
	
	public List<String> getKeyValues() {
		return keyValues;
	}
	
	public void setKeyValues(List<String> keyValues) {
		this.keyValues = keyValues;
	}
	
	public void setKeyValues(String[] keyValues) {
		this.keyValues = toStringList(keyValues);
	}
	
	public List<String> getQualifiers() {
		return qualifiers;
	}
	
	public void setQualifiers(String[] qualifierValues) {
		this.qualifiers = toStringList(qualifierValues);
	}
	
	public void setQualifiers(List<String> qualifiers) {
		this.qualifiers = qualifiers;
	}
	
	public List<String> getSummaryValues() {
		return summaryValues;
	}
	
	public void setSummaryValues(String[] summaryValues) {
		this.summaryValues = toStringList(summaryValues);
	}
	
	public void setSummaryValues(List<String> summaryValues) {
		this.summaryValues = summaryValues;
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
	
	public Set<Integer> getRecordIds() {
		return recordIds;
	}
	
	public void setRecordIds(Set<Integer> recordIds) {
		this.recordIds = recordIds;
	}
	
	private static List<String> toStringList(String[] values) {
		if (values == null || values.length == 0) {
			return Collections.emptyList();
		} else {
			return Arrays.asList(values);
		}
	}
}
