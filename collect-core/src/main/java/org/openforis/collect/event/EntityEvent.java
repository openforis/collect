package org.openforis.collect.event;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openforis.idm.metamodel.validation.ValidationResultFlag;

public abstract class EntityEvent extends RecordEvent {

	private Map<Integer, Boolean> relevanceByChildDefinitionId;
	private Map<Integer, Integer> minCountByChildDefinitionId;
	private Map<Integer, Integer> maxCountByChildDefinitionId;
	private Map<Integer, ValidationResultFlag> minCountValidationByChildDefinitionId;
	private Map<Integer, ValidationResultFlag> maxCountValidationByChildDefinitionId;
	
	public EntityEvent(String surveyName, Integer recordId,
			RecordStep step, String definitionId, List<String> ancestorIds,
			String nodeId, Date timestamp, String userName) {
		super(surveyName, recordId, step, definitionId, ancestorIds, nodeId,
				timestamp, userName);
	}

	public Map<Integer, Boolean> getRelevanceByChildDefinitionId() {
		return relevanceByChildDefinitionId;
	}
	
	public void setRelevanceByChildDefinitionId(Map<Integer, Boolean> relevanceByChildDefinitionId) {
		this.relevanceByChildDefinitionId = relevanceByChildDefinitionId;
	}
	
	public Map<Integer, Integer> getMinCountByChildDefinitionId() {
		return minCountByChildDefinitionId;
	}
	
	public void setMinCountByChildDefinitionId(Map<Integer, Integer> minCountByChildDefinitionId) {
		this.minCountByChildDefinitionId = minCountByChildDefinitionId;
	}
	
	public Map<Integer, Integer> getMaxCountByChildDefinitionId() {
		return maxCountByChildDefinitionId;
	}
	
	public void setMaxCountByChildDefinitionId(Map<Integer, Integer> maxCountByChildDefinitionId) {
		this.maxCountByChildDefinitionId = maxCountByChildDefinitionId;
	}
	
	public Map<Integer, ValidationResultFlag> getMinCountValidationByChildDefinitionId() {
		return minCountValidationByChildDefinitionId;
	}
	
	public void setMinCountValidationByChildDefinitionId(Map<Integer, ValidationResultFlag> minCountValidationByChildDefinitionId) {
		this.minCountValidationByChildDefinitionId = minCountValidationByChildDefinitionId;
	}
	
	public Map<Integer, ValidationResultFlag> getMaxCountValidationByChildDefinitionId() {
		return maxCountValidationByChildDefinitionId;
	}
	
	public void setMaxCountValidationByChildDefinitionId(Map<Integer, ValidationResultFlag> maxCountValidationByChildDefinitionId) {
		this.maxCountValidationByChildDefinitionId = maxCountValidationByChildDefinitionId;
	}
}
