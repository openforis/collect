package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

import org.openforis.idm.metamodel.validation.ValidationResultFlag;

public class NodeMaxCountValidationUpdatedEvent extends NodeCountValidationUpdatedEvent {

	public NodeMaxCountValidationUpdatedEvent(String surveyName, Integer recordId, RecordStep recordStep,
			String definitionId, List<String> ancestorIds, String nodeId, Date timestamp, String userName,
			int childDefinitionId, ValidationResultFlag flag) {
		super(surveyName, recordId, recordStep, definitionId, ancestorIds, nodeId, timestamp, userName,
				childDefinitionId, flag);
	}

}
