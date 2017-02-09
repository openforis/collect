package org.openforis.collect.command.handler;

import java.util.List;

import org.openforis.collect.command.AddNodeCommand;
import org.openforis.collect.event.EventProducer;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordProvider;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Entity;

public class AddNodeCommandHandler<C extends AddNodeCommand> extends NodeCommandHandler<C> {

	public AddNodeCommandHandler(SurveyManager surveyManager, RecordProvider recordProvider, RecordManager recordManager) {
		super(surveyManager, recordProvider, recordManager);
	}

	@Override
	public List<RecordEvent> execute(C command) {
		CollectRecord record = findRecord(command);
		Entity parentEntity = (Entity) record.getNodeByInternalId(command.getParentEntityId());
		NodeDefinition nodeDef = parentEntity.getDefinition().getChildDefinition(command.getNodeDefId());
		NodeChangeSet changeSet = recordUpdater.addNode(parentEntity, nodeDef);
		
		recordManager.save(record);
		
		List<RecordEvent> events = new EventProducer().produceFor(changeSet, command.getUsername());
		return events;
	}
	
}
