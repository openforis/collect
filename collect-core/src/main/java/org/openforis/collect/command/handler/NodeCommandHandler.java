package org.openforis.collect.command.handler;

import java.util.List;

import org.openforis.collect.command.NodeCommand;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordProvider;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Value;

public abstract class NodeCommandHandler<C extends NodeCommand> implements CommandHandler<List<RecordEvent>, C> {

	protected SurveyManager surveyManager;
	protected RecordProvider recordProvider;
	protected RecordManager recordManager;
	protected RecordUpdater recordUpdater = new RecordUpdater();
	
	public NodeCommandHandler(SurveyManager surveyManager, RecordProvider recordProvider, RecordManager recordManager) {
		this.surveyManager = surveyManager;
		this.recordProvider = recordProvider;
		this.recordManager = recordManager;
	}

	protected Attribute<?, Value> findAttribute(NodeCommand command) {
		CollectRecord record = findRecord(command);
		return findAttribute(command, record);
	}

	@SuppressWarnings("unchecked")
	protected Attribute<?, Value> findAttribute(NodeCommand command, CollectRecord record) {
		if (command.getNodeId() != null) {
			return (Attribute<?, Value>) record.getNodeByInternalId(command.getNodeId());
		}
		return record.findNodeByPath(command.getNodePath());
	}

	protected CollectRecord findRecord(NodeCommand command) {
		CollectSurvey survey = surveyManager.getById(command.getSurveyId());
		CollectRecord record = recordProvider.provide(survey, command.getRecordId());
		return record;
	}
	
	protected Entity findParentEntity(NodeCommand command) {
		CollectRecord record = findRecord(command);
		Entity parentEntity = record.findNodeByPath(command.getParentEntityPath());
		return parentEntity;
	}

}