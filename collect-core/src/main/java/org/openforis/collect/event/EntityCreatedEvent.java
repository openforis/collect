package org.openforis.collect.event;

import java.util.Map;

import org.openforis.idm.metamodel.validation.ValidationResultFlag;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class EntityCreatedEvent extends EntityEvent {

	private Map<Integer, Boolean> childrenRelevanceByDefinitionId;
	private Map<Integer, Integer> childrenMinCountByDefinitionId;
	private Map<Integer, Integer> childrenMaxCountByDefinitionId;
	private Map<Integer, ValidationResultFlag> childrenMinCountValidationByDefinitionId;
	private Map<Integer, ValidationResultFlag> childrenMaxCountValidationByDefinitionId;

	public Map<Integer, Boolean> getChildrenRelevanceByDefinitionId() {
		return childrenRelevanceByDefinitionId;
	}
	
	public void setChildrenRelevanceByDefinitionId(Map<Integer, Boolean> childrenRelevanceByDefinitionId) {
		this.childrenRelevanceByDefinitionId = childrenRelevanceByDefinitionId;
	}
	
	public Map<Integer, Integer> getChildrenMinCountByDefinitionId() {
		return childrenMinCountByDefinitionId;
	}

	public void setChildrenMinCountByDefinitionId(Map<Integer, Integer> childrenMinCountByDefinitionId) {
		this.childrenMinCountByDefinitionId = childrenMinCountByDefinitionId;
	}

	public Map<Integer, Integer> getChildrenMaxCountByDefinitionId() {
		return childrenMaxCountByDefinitionId;
	}

	public void setChildrenMaxCountByDefinitionId(Map<Integer, Integer> childrenMaxCountByDefinitionId) {
		this.childrenMaxCountByDefinitionId = childrenMaxCountByDefinitionId;
	}

	public Map<Integer, ValidationResultFlag> getChildrenMinCountValidationByDefinitionId() {
		return childrenMinCountValidationByDefinitionId;
	}

	public void setChildrenMinCountValidationByDefinitionId(
			Map<Integer, ValidationResultFlag> childrenMinCountValidationByDefinitionId) {
		this.childrenMinCountValidationByDefinitionId = childrenMinCountValidationByDefinitionId;
	}

	public Map<Integer, ValidationResultFlag> getChildrenMaxCountValidationByDefinitionId() {
		return childrenMaxCountValidationByDefinitionId;
	}

	public void setChildrenMaxCountValidationByDefinitionId(
			Map<Integer, ValidationResultFlag> childrenMaxCountValidationByDefinitionId) {
		this.childrenMaxCountValidationByDefinitionId = childrenMaxCountValidationByDefinitionId;
	}
}
