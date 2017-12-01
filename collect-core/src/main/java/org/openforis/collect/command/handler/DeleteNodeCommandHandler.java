package org.openforis.collect.command.handler;

import java.util.List;

import org.openforis.collect.command.DeleteNodeCommand;
import org.openforis.collect.event.EventProducer;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordProvider;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Value;

public class DeleteNodeCommandHandler<C extends DeleteNodeCommand> extends NodeCommandHandler<C> {

	public DeleteNodeCommandHandler(SurveyManager surveyManager, RecordProvider recordProvider, RecordManager recordManager) {
		super(surveyManager, recordProvider, recordManager);
	}

	@Override
	public List<RecordEvent> execute(C command) {
		CollectRecord record = findRecord(command);
		Attribute<?, Value> attribute = findAttribute(command, record);
		NodeChangeSet changeSet = recordUpdater.deleteNode(attribute);
		
		recordManager.save(record);
		
		List<RecordEvent> events = new EventProducer().produceFor(changeSet, command.getUsername());
		return events;
	}
	
}
