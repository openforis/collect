package org.openforis.collect.event;


import static java.util.Arrays.asList;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openforis.collect.model.AttributeChange;
import org.openforis.collect.model.EntityAddChange;
import org.openforis.collect.model.NodeChange;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.collect.model.NodeDeleteChange;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition.Type;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.BooleanAttribute;
import org.openforis.idm.model.BooleanValue;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.DateAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NumberAttribute;
import org.openforis.idm.model.NumericRangeAttribute;
import org.openforis.idm.model.TaxonAttribute;
import org.openforis.idm.model.TextAttribute;
import org.openforis.idm.model.Time;
import org.openforis.idm.model.TimeAttribute;
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
		notifyListeners(toEvents(changeSet, userName));
	}

	private List<RecordEvent> toEvents(NodeChangeSet changeSet, String userName) {
		List<RecordEvent> events = new ArrayList<RecordEvent>();
		List<NodeChange<?>> changes = changeSet.getChanges();
		for (NodeChange<?> change : changes) {
			events.addAll(toEvent(change, userName));
		}
		return events;
	}

	private List<? extends RecordEvent> toEvent(NodeChange<?> change, String userName) {
		Node<?> node = change.getNode();
		Integer recordId = change.getRecordId();
		Integer parentId = change.getParentId();

		Survey survey = node.getSurvey();
		String surveyName = survey.getName();
		NodeDefinition nodeDef = node.getDefinition();
		int definitionId = nodeDef.getId();
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
			if (node instanceof BooleanAttribute) {
				BooleanValue value = ((BooleanAttribute) node).getValue();
				return asList(new BooleanAttributeUpdatedEvent(surveyName, recordId, definitionId, parentId, nodeId, value.getValue(), timestamp, userName));
			} else if (node instanceof CodeAttribute) {
				Code value = ((CodeAttribute) node).getValue();
				return asList(new CodeAttributeUpdatedEvent(surveyName, recordId, definitionId, parentId, nodeId, value.getCode(), 
						value.getQualifier(), timestamp, userName));
			} else if (node instanceof CoordinateAttribute) {
				Coordinate value = ((CoordinateAttribute) node).getValue();
				return asList(new CoordinateAttributeUpdatedEvent(surveyName, recordId, definitionId, parentId, nodeId, value.getX(), value.getY(), value.getSrsId(), 
						timestamp, userName));
			} else if (node instanceof DateAttribute) {
				org.openforis.idm.model.Date value = ((DateAttribute) node).getValue();
				return asList(new DateAttributeUpdatedEvent(surveyName, recordId, definitionId, parentId, nodeId, value.toJavaDate(), timestamp, userName));
			//TODO
//			} else if (node instanceof FileAttribute) {
			} else if (node instanceof NumberAttribute<?, ?>) {
				NumberAttribute<?, ?> attribute = (NumberAttribute<?, ?>) node;
				Number value = attribute.getNumber();
				Integer unitId = attribute.getUnitId();
				Type valueType = ((NumericAttributeDefinition) nodeDef).getType();
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
			} else if (node instanceof NumericRangeAttribute<?, ?>) {
				NumericRangeAttribute<?, ?> attribute = (NumericRangeAttribute<?, ?>) node;
				Number from = attribute.getFrom();
				Number to = attribute.getTo();
				Integer unitId = attribute.getUnitId();
				Type valueType = ((RangeAttributeDefinition) nodeDef).getType();
				switch(valueType) {
				case INTEGER:
					return asList(new IntegerRangeAttributeUpdatedEvent(surveyName, recordId, definitionId, parentId, nodeId, 
							(Integer) from, (Integer) to, unitId, timestamp, userName));
				case REAL:
					return asList(new DoubleRangeAttributeUpdatedEvent(surveyName, recordId, definitionId, parentId, nodeId, 
							(Double) from, (Double) to, unitId, timestamp, userName));
				default:
					throw new IllegalArgumentException("Numeric type not supported: " + valueType);
				}
			} else if (node instanceof TaxonAttribute) {
				TaxonAttribute taxonAttr = (TaxonAttribute) node;
				new TaxonAttributeUpdatedEvent(surveyName, recordId, definitionId, parentId, nodeId, 
						taxonAttr.getCode(), taxonAttr.getScientificName(), taxonAttr.getVernacularName(), taxonAttr.getLanguageCode(), 
						taxonAttr.getLanguageVariety(), timestamp, userName);
			} else if (node instanceof TextAttribute) {
				String text = ((TextAttribute) node).getText();
				return asList(new TextAttributeUpdatedEvent(surveyName, recordId, definitionId, parentId, nodeId, text, timestamp, userName));
			} else if (node instanceof TimeAttribute) {
				Time value = ((TimeAttribute) node).getValue();
				return asList(new DateAttributeUpdatedEvent(surveyName, recordId, definitionId, parentId, nodeId, value.toJavaDate(), timestamp, userName));
			}
		} else if (change instanceof NodeDeleteChange) {
			if (node instanceof Entity) {
				return asList(new EntityDeletedEvent(surveyName, recordId, definitionId, 
						parentId, nodeId, timestamp, userName));
			} else {
				//TODO
			}
		}
		return emptyList();
	}

	private void notifyListeners(List<RecordEvent> events) {
		for (EventListener listener : listeners) {
			listener.onEvents(events);
		}
	}

}
