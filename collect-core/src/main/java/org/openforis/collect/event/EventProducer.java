package org.openforis.collect.event;


import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.openforis.collect.model.AttributeAddChange;
import org.openforis.collect.model.AttributeChange;
import org.openforis.collect.model.EntityAddChange;
import org.openforis.collect.model.NodeChange;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.collect.model.NodeDeleteChange;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
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

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
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
		List<String> ancestorIds = getAncestorIds(change);
		Integer recordId = change.getRecordId();

		Survey survey = node.getSurvey();
		String surveyName = survey.getName();
		NodeDefinition nodeDef = node.getDefinition();
		int definitionId = nodeDef.getId();
		int nodeId = node.getInternalId();
		Date timestamp = new Date();
		
		if (change instanceof EntityAddChange) {
			List<RecordEvent> events = new ArrayList<RecordEvent>();
			Entity entity = (Entity) node;
			if (entity.isRoot()) {
				events.add(new RootEntityCreatedEvent(surveyName, recordId, String.valueOf(definitionId), 
						String.valueOf(nodeId), timestamp, userName));
			} else {
				 events.add(new EntityCreatedEvent(surveyName, recordId, String.valueOf(definitionId), 
						ancestorIds, String.valueOf(nodeId), timestamp, userName));
			}
			//add node collection created events
			for (NodeDefinition childDef : ((EntityDefinition) nodeDef).getChildDefinitions()) {
				if (childDef.isMultiple()) {
					String collectionId = getNodeCollectionId(nodeId, childDef);
					String collectionDefId = getNodeCollectionDefinitionId(entity.getDefinition(), childDef);
					if (childDef instanceof AttributeDefinition) {
						events.add(new AttributeCollectionCreatedEvent(surveyName, recordId, collectionDefId, 
								ancestorIds, collectionId, timestamp, userName));
					} else {
						events.add(new EntityCollectionCreatedEvent(surveyName, recordId, collectionDefId, 
								ancestorIds, collectionId, timestamp, userName));
					}
				}
			}
			return events;
		} else if (change instanceof AttributeChange) {
			List<RecordEvent> result = new ArrayList<RecordEvent>();
			if (change instanceof AttributeAddChange) {
				result.add(new AttributeCreatedEvent(surveyName, recordId,  String.valueOf(definitionId), 
						ancestorIds, String.valueOf(nodeId), timestamp, userName));
			}
			AttributeUpdatedEvent event = toAttributeUpdatedEvent(change, userName);
			if (event != null) { //TODO skip it, always return an event
				result.add(event);
			}
			return result;
		} else if (change instanceof NodeDeleteChange) {
			if (node instanceof Entity) {
				return asList(new EntityDeletedEvent(surveyName, recordId,  String.valueOf(definitionId), 
						ancestorIds, String.valueOf(nodeId), timestamp, userName));
			} else {
				return asList(new AttributeDeletedEvent(surveyName, recordId,  String.valueOf(definitionId),
						ancestorIds, String.valueOf(nodeId), timestamp, userName));	
			}
		}
		return emptyList();
	}

	private List<String> getAncestorIds(NodeChange<?> nodeChange) {
		NodeDefinition nodeDef = nodeChange.getNode().getDefinition();
		if (nodeDef instanceof EntityDefinition && ((EntityDefinition) nodeDef).isRoot()) {
			return Collections.emptyList();
		}
		List<Integer> ancestorEntityIds = nodeChange.getAncestorIds();
		List<String> ancestorIds = new ArrayList<String>();
		if (nodeDef.isMultiple()) {
			int parentId = ancestorEntityIds.get(0);
			ancestorIds.add(getNodeCollectionId(parentId, nodeDef));
		}
		
		List<EntityDefinition> ancestorDefs = nodeDef.getAncestorEntityDefinitions();
		for (int ancestorIdx = 0; ancestorIdx < ancestorEntityIds.size(); ancestorIdx++) {
			int ancestorEntityId = ancestorEntityIds.get(ancestorIdx);
			EntityDefinition ancestorDef = ancestorDefs.get(ancestorIdx);
			ancestorIds.add(String.valueOf(ancestorEntityId));
			boolean inCollection = ! ancestorDef.isRoot() && ancestorDef.isMultiple();
			if (inCollection) {
				Integer ancestorParentId = ancestorEntityIds.get(ancestorIdx + 1);
				ancestorIds.add(getNodeCollectionId(ancestorParentId, ancestorDef)); 
			}
		}
		return ancestorIds;
	}

	private String getNodeCollectionId(int parentId, NodeDefinition memberDef) {
		return parentId + "|" + memberDef.getId();
	}
	
	private String getNodeCollectionDefinitionId(EntityDefinition parentDef, NodeDefinition memberDef) {
		return parentDef.getId() + "|" + memberDef.getId();
	}	

	private AttributeUpdatedEvent toAttributeUpdatedEvent(NodeChange<?> change,
			String userName) {
		Node<?> node = change.getNode();
		Integer recordId = change.getRecordId();
		List<String> ancestorIds = getAncestorIds(change);
		
		Survey survey = node.getSurvey();
		String surveyName = survey.getName();
		NodeDefinition nodeDef = node.getDefinition();
		int definitionId = nodeDef.getId();
		int nodeId = node.getInternalId();
		Date timestamp = new Date();
		
		AttributeUpdatedEvent event = null;
		if (node instanceof BooleanAttribute) {
			BooleanValue value = ((BooleanAttribute) node).getValue();
			event = new BooleanAttributeUpdatedEvent(surveyName, recordId, String.valueOf(definitionId), 
					ancestorIds, String.valueOf(nodeId), value.getValue(), timestamp, userName);
		} else if (node instanceof CodeAttribute) {
			Code value = ((CodeAttribute) node).getValue();
			event = new CodeAttributeUpdatedEvent(surveyName, recordId, String.valueOf(definitionId), 
					ancestorIds, String.valueOf(nodeId), value.getCode(), 
					value.getQualifier(), timestamp, userName);
		} else if (node instanceof CoordinateAttribute) {
			Coordinate value = ((CoordinateAttribute) node).getValue();
			event = new CoordinateAttributeUpdatedEvent(surveyName, recordId, String.valueOf(definitionId), 
					ancestorIds, String.valueOf(nodeId), value.getX(), value.getY(), value.getSrsId(), 
					timestamp, userName);
		} else if (node instanceof DateAttribute) {
			org.openforis.idm.model.Date value = ((DateAttribute) node).getValue();
			event = new DateAttributeUpdatedEvent(surveyName, recordId, String.valueOf(definitionId), 
					ancestorIds, String.valueOf(nodeId), value.toJavaDate(), timestamp, userName);
		//TODO
//			} else if (node instanceof FileAttribute) {
		} else if (node instanceof NumberAttribute<?, ?>) {
			NumberAttribute<?, ?> attribute = (NumberAttribute<?, ?>) node;
			Number value = attribute.getNumber();
			Integer unitId = attribute.getUnitId();
			Type valueType = ((NumericAttributeDefinition) nodeDef).getType();
			switch(valueType) {
			case INTEGER:
				event = new IntegerAttributeUpdatedEvent(surveyName, recordId, String.valueOf(definitionId), 
						ancestorIds, String.valueOf(nodeId), 
						(Integer) value, unitId, timestamp, userName);
				break;
			case REAL:
				event = new DoubleAttributeUpdatedEvent(surveyName, recordId, String.valueOf(definitionId), 
						ancestorIds, String.valueOf(nodeId), 
						(Double) value, unitId, timestamp, userName);
				break;
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
				event = new IntegerRangeAttributeUpdatedEvent(surveyName, recordId, String.valueOf(definitionId), 
						ancestorIds, String.valueOf(nodeId), 
						(Integer) from, (Integer) to, unitId, timestamp, userName);
				break;
			case REAL:
				event = new DoubleRangeAttributeUpdatedEvent(surveyName, recordId, String.valueOf(definitionId), 
						ancestorIds, String.valueOf(nodeId), 
						(Double) from, (Double) to, unitId, timestamp, userName);
				break;
			default:
				throw new IllegalArgumentException("Numeric type not supported: " + valueType);
			}
		} else if (node instanceof TaxonAttribute) {
			TaxonAttribute taxonAttr = (TaxonAttribute) node;
			event = new TaxonAttributeUpdatedEvent(surveyName, recordId, String.valueOf(definitionId), 
					ancestorIds, String.valueOf(nodeId), 
					taxonAttr.getCode(), taxonAttr.getScientificName(), taxonAttr.getVernacularName(), taxonAttr.getLanguageCode(), 
					taxonAttr.getLanguageVariety(), timestamp, userName);
		} else if (node instanceof TextAttribute) {
			String text = ((TextAttribute) node).getText();
			event = new TextAttributeUpdatedEvent(surveyName, recordId, String.valueOf(definitionId), 
					ancestorIds, String.valueOf(nodeId), text, timestamp, userName);
		} else if (node instanceof TimeAttribute) {
			Time value = ((TimeAttribute) node).getValue();
			event = new DateAttributeUpdatedEvent(surveyName, recordId, String.valueOf(definitionId), 
					ancestorIds, String.valueOf(nodeId), value.toJavaDate(), timestamp, userName);
//		} else {
//			throw new IllegalArgumentException("Unexpected node type: " + node.getClass().getSimpleName());
		}
		return event;
	}

	private void notifyListeners(List<RecordEvent> events) {
		for (EventListener listener : listeners) {
			listener.onEvents(events);
		}
	}

}
