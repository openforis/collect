package org.openforis.collect.command.handler;

import org.openforis.collect.command.NodeCommand;
import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordProvider;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Value;

public abstract class NodeCommandHandler<C extends NodeCommand> implements CommandHandler<C> {

	protected SurveyManager surveyManager;
	protected RecordProvider recordProvider;
	protected RecordManager recordManager;
	protected RecordUpdater recordUpdater = new RecordUpdater();
	protected MessageSource messageSource;
	
	public NodeCommandHandler(SurveyManager surveyManager, RecordProvider recordProvider, RecordManager recordManager, MessageSource messageSource) {
		this.surveyManager = surveyManager;
		this.recordProvider = recordProvider;
		this.recordManager = recordManager;
		this.messageSource = messageSource;
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