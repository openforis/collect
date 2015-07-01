package org.openforis.collect.event;


import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openforis.collect.model.AttributeChange;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.EntityAddChange;
import org.openforis.collect.model.NodeChange;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition.Type;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NumberAttribute;
import org.openforis.idm.model.TextAttribute;
import org.springframework.stereotype.Component;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
@Component
public class EventProducer implements EventSource {

	private final List<EventListener> listeners;

	public EventProducer(List<EventListener> listeners) {
		super();
		this.listeners = listeners;
	}

	@Override
	public void register(EventListener listener) {
		listeners.add(listener);
	}

	public void produceFor(NodeChangeSet changeSet, String userName) {
		List<RecordEvent> events = new ArrayList<RecordEvent>();
		List<NodeChange<?>> changes = changeSet.getChanges();
		for (NodeChange<?> change : changes) {
			events.addAll(toEvent(change, userName));
		}
		notifyListeners(events);
	}

	private List<? extends RecordEvent> toEvent(NodeChange<?> change, String userName) {
		Node<?> node = change.getNode();
		Survey survey = node.getSurvey();
		String surveyName = survey.getName();
		Integer recordId = node.getRecord().getId();
		NodeDefinition nodeDef = node.getDefinition();
		int definitionId = nodeDef.getId();
		Integer parentId = node.getParent() == null ? null : node.getParent().getInternalId();
		int nodeId = node.getInternalId();
		Date timestamp = new Date();
		
		if (change instanceof EntityAddChange) {
			Entity entity = (Entity) change.getNode();
			if (entity.isRoot()) {
				return asList(new RootEntityCreatedEvent(surveyName, recordId, definitionId, 
						nodeId, timestamp, userName));
			} else {
				return asList(new EntityCreatedEvent(surveyName, recordId, definitionId, 
						parentId, nodeId, timestamp, userName));
			}
		} else if (change instanceof AttributeChange) {
			if (node instanceof CodeAttribute) {
				Code value = ((CodeAttribute) node).getValue();
				return asList(new CodeAttributeUpdatedEvent(surveyName, recordId, definitionId, parentId, nodeId, value.getCode(), 
						value.getQualifier(), timestamp, userName));
			} else if (node instanceof NumberAttribute<?, ?>) {
				Type valueType = ((NumericAttributeDefinition) nodeDef).getType();
				NumberAttribute<?, ?> attribute = (NumberAttribute<?, ?>) node;
				Number value = attribute.getNumber();
				Integer unitId = attribute.getUnitId();
				switch(valueType) {
				case INTEGER:
					return asList(new IntegerAttributeUpdatedEvent(surveyName, recordId, definitionId, parentId, nodeId, 
							(Integer) value, unitId, timestamp, userName));
				case REAL:
					return asList(new DoubleAttributeUpdatedEvent(surveyName, recordId, definitionId, parentId, nodeId, 
							(Double) value, unitId, timestamp, userName));
				default:
					throw new IllegalArgumentException("Numeric type not supported: " + valueType);
				}
			} else if (node instanceof TextAttribute) {
				String text = ((TextAttribute) node).getText();
				return asList(new TextAttributeUpdatedEvent(surveyName, recordId, definitionId, parentId, nodeId, text, timestamp, userName));
			}
		}
		return emptyList();
	}

	private void notifyListeners(List<RecordEvent> events) {
		for (EventListener listener : listeners) {
			listener.onEvents(events);
		}
	}

	public void produceForNew(CollectRecord record) {
		
	}

	public void produceForDeleted(CollectRecord record) {
		
	}
	
	

}
