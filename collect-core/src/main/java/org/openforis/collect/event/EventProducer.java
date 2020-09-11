package org.openforis.collect.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.model.AttributeAddChange;
import org.openforis.collect.model.AttributeChange;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.EntityAddChange;
import org.openforis.collect.model.EntityChange;
import org.openforis.collect.model.NodeChange;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.collect.model.NodeDeleteChange;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition.Type;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.BooleanAttribute;
import org.openforis.idm.model.BooleanValue;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.DateAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodeVisitor;
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
public class EventProducer {

	private EventProducerContext context;
	private EventListener consumer;

	public EventProducer(EventProducerContext context, EventListener consumer) {
		super();
		this.context = context;
		this.consumer = consumer;
	}
	
	public void produceFor(Object obj) {
		if (obj instanceof CollectRecord) {
			produceFor((CollectRecord) obj);
		} else if (obj instanceof NodeChangeSet) {
			produceFor((NodeChangeSet) obj);
		} else {
			throw new IllegalArgumentException("Cannot produce events for object of type " + obj.getClass().getName());
		}
	}
	
	public void produceFor(CollectRecord record) {
		final Integer recordId = record.getId();
		final RecordStep recordStep = record.getStep().toRecordStep();

		record.getRootEntity().traverse(new NodeVisitor() {
			public void visit(Node<? extends NodeDefinition> node, int idx) {
				NodeDefinition nodeDef = node.getDefinition();
				List<String> ancestorIds = getAncestorIds(nodeDef, node.getAncestorIds());
				EventFactory factory = new EventFactory(recordId, recordStep, ancestorIds, node);
				if (node instanceof Entity) {
					Entity entity = (Entity) node;
					factory.entityCreated(entity.getRelevanceByDefinitionId(), entity.getMinCountByDefinitionId(),
							entity.getMaxCountByDefinitionId(), entity.getMinCountValidationResultByDefinitionId(),
							entity.getMaxCountValidationResultByDefinitionId());
				} else if (node instanceof Attribute) {
					if (nodeDef.isMultiple()) {
						factory.attributeCreated();
					} else {
						factory.attributeUpdated();
					}
				}
			}
		});
	}

	public void produceFor(NodeChangeSet changeSet) {
		notifyEvents(changeSet);
	}

	private void notifyEvents(NodeChangeSet changeSet) {
		for (NodeChange<?> change : changeSet.getChanges()) {
			notifyEvents(change);
		}
	}

	private void notifyEvents(NodeChange<?> change) {
		Node<?> node = change.getNode();
		List<String> ancestorIds = getAncestorIds(node.getDefinition(), change.getAncestorIds());
		Integer recordId = change.getRecordId();
		RecordStep recordStep = change.getRecordStep().toRecordStep();

		EventFactory factory = new EventFactory(recordId, recordStep, ancestorIds, node);

		if (change instanceof EntityChange) {
			EntityChange entityChange = (EntityChange) change;
			if (change instanceof EntityAddChange) {
				factory.entityCreated(entityChange.getChildrenRelevance(),
						entityChange.getMinCountByChildDefinitionId(), entityChange.getMaxCountByChildDefinitionId(),
						entityChange.getChildrenMinCountValidation(), entityChange.getChildrenMaxCountValidation());
			} else {
				factory.entityUpdated(entityChange.getChildrenRelevance(),
						entityChange.getMinCountByChildDefinitionId(), entityChange.getMaxCountByChildDefinitionId(),
						entityChange.getChildrenMinCountValidation(), entityChange.getChildrenMaxCountValidation());
			}
		} else if (change instanceof AttributeChange) {
			if (change instanceof AttributeAddChange) {
				factory.attributeCreated();
			} else {
				factory.attributeUpdated();
			}
		} else if (change instanceof NodeDeleteChange) {
			if (node instanceof Entity) {
				factory.entityDeleted();
			} else {
				factory.attributeDeleted();
			}
		}

	}

	private List<String> getAncestorIds(NodeDefinition nodeDef, List<Integer> ancestorEntityIds) {
		if (nodeDef instanceof EntityDefinition && ((EntityDefinition) nodeDef).isRoot()) {
			return Collections.emptyList();
		}
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
			boolean inCollection = !ancestorDef.isRoot() && ancestorDef.isMultiple();
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

	private class EventFactory {
		Integer recordId;
		RecordStep recordStep;
		List<String> ancestorIds;
		Node<?> node;
		Date timestamp;

		EventFactory(Integer recordId, RecordStep recordStep, List<String> ancestorIds,
				Node<?> node) {
			this.recordId = recordId;
			this.recordStep = recordStep;
			this.ancestorIds = ancestorIds;
			this.node = node;
			this.timestamp = new Date();
		}

		void entityCreated(Map<Integer, Boolean> relevanceByChildDefinitionId,
				Map<Integer, Integer> minCountByChildDefinitionId, Map<Integer, Integer> maxCountByChildDefinitionId,
				Map<Integer, ValidationResultFlag> minCountValidationByChildDefinitionId,
				Map<Integer, ValidationResultFlag> maxCountValidationByChildDefinitionId) {
			Entity entity = (Entity) node;
			EntityCreatedEvent entityEvent = entity.isRoot() ? new RootEntityCreatedEvent() : new EntityCreatedEvent();
			fillRecordEvent(entityEvent);
			entityEvent.setChildrenRelevanceByDefinitionId(relevanceByChildDefinitionId);
			entityEvent.setChildrenMinCountByDefinitionId(minCountByChildDefinitionId);
			entityEvent.setChildrenMaxCountByDefinitionId(maxCountByChildDefinitionId);
			entityEvent.setChildrenMinCountValidationByDefinitionId(minCountValidationByChildDefinitionId);
			entityEvent.setChildrenMaxCountValidationByDefinitionId(maxCountValidationByChildDefinitionId);
			consumer.onEvent(entityEvent);

			// add node collection created events
			EntityDefinition entityDef = (EntityDefinition) node.getDefinition();
			for (NodeDefinition childDef : entityDef.getChildDefinitions()) {
				if (childDef.isMultiple()) {
					String collectionId = getNodeCollectionId(entityDef.getId(), childDef);
					String collectionDefId = getNodeCollectionDefinitionId(entity.getDefinition(), childDef);
					RecordEvent event = childDef instanceof AttributeDefinition ? new AttributeCollectionCreatedEvent()
							: new EntityCollectionCreatedEvent();
					event = fillRecordEvent(event);
					event.setDefinitionId(collectionDefId);
					event.setNodeId(collectionId);
					consumer.onEvent(event);
				}
			}
		}

		void entityUpdated(Map<Integer, Boolean> relevanceByChildDefinitionId,
				Map<Integer, Integer> minCountByChildDefinitionId, Map<Integer, Integer> maxCountByChildDefinitionId,
				Map<Integer, ValidationResultFlag> minCountValidationByChildDefinitionId,
				Map<Integer, ValidationResultFlag> maxCountValidationByChildDefinitionId) {
			for (Entry<Integer, Boolean> entry : relevanceByChildDefinitionId.entrySet()) {
				relevanceUpdated(entry.getKey(), entry.getValue());
			}
			for (Entry<Integer, Integer> entry : minCountByChildDefinitionId.entrySet()) {
				minCountUpdated(entry.getKey(), entry.getValue());
			}
			for (Entry<Integer, Integer> entry : maxCountByChildDefinitionId.entrySet()) {
				maxCountUpdated(entry.getKey(), entry.getValue());
			}
			for (Entry<Integer, ValidationResultFlag> entry : minCountValidationByChildDefinitionId.entrySet()) {
				minCountValidationUpdated(entry.getKey(), entry.getValue());
			}
			for (Entry<Integer, ValidationResultFlag> entry : maxCountValidationByChildDefinitionId.entrySet()) {
				maxCountValidationUpdated(entry.getKey(), entry.getValue());
			}
		}

		void attributeCreated() {
			consumer.onEvent(fillRecordEvent(new AttributeCreatedEvent()));
			if (node.hasData()) {
				attributeUpdated();
			}
		}

		@SuppressWarnings("unchecked")
		void attributeUpdated() {
			AttributeUpdatedEvent event = null;
			if (node instanceof BooleanAttribute) {
				event = new BooleanAttributeUpdatedEvent();
				BooleanValue value = ((BooleanAttribute) node).getValue();
				((BooleanAttributeUpdatedEvent) event).setValue(value.getValue());
			} else if (node instanceof CodeAttribute) {
				event = new CodeAttributeUpdatedEvent();
				Code value = ((CodeAttribute) node).getValue();
				((CodeAttributeUpdatedEvent) event).setCode(value.getCode());
				((CodeAttributeUpdatedEvent) event).setQualifier(value.getQualifier());
			} else if (node instanceof CoordinateAttribute) {
				event = new CoordinateAttributeUpdatedEvent();
				Coordinate value = ((CoordinateAttribute) node).getValue();
				((CoordinateAttributeUpdatedEvent) event).setX(value.getX());
				((CoordinateAttributeUpdatedEvent) event).setY(value.getY());
				((CoordinateAttributeUpdatedEvent) event).setSrsId(value.getSrsId());
			} else if (node instanceof DateAttribute) {
				org.openforis.idm.model.Date value = ((DateAttribute) node).getValue();
				event = new DateAttributeUpdatedEvent();
				((DateAttributeUpdatedEvent) event).setDate(value.toJavaDate());
				// TODO
//				} else if (node instanceof FileAttribute) {
			} else if (node instanceof NumberAttribute<?, ?>) {
				NumberAttribute<?, ?> attribute = (NumberAttribute<?, ?>) node;
				Number value = attribute.getNumber();
				Type valueType = ((NumericAttributeDefinition) node.getDefinition()).getType();
				switch (valueType) {
				case INTEGER:
					event = new IntegerAttributeUpdatedEvent();
					break;
				case REAL:
					event = new DoubleAttributeUpdatedEvent();
					break;
				default:
					throw new IllegalArgumentException("Numeric type not supported: " + valueType);
				}
				((NumberAttributeUpdatedEvent<?>) event).setUnitId(attribute.getUnitId());
				((NumberAttributeUpdatedEvent<Number>) event).setValue(value);

			} else if (node instanceof NumericRangeAttribute<?, ?>) {
				NumericRangeAttribute<?, ?> attribute = (NumericRangeAttribute<?, ?>) node;
				Number from = attribute.getFrom();
				Number to = attribute.getTo();
				Type valueType = ((RangeAttributeDefinition) node.getDefinition()).getType();
				switch (valueType) {
				case INTEGER:
					event = new IntegerRangeAttributeUpdatedEvent();
					break;
				case REAL:
					event = new DoubleRangeAttributeUpdatedEvent();
					break;
				default:
					throw new IllegalArgumentException("Numeric type not supported: " + valueType);
				}
				((RangeAttributeUpdatedEvent<?>) event).setUnitId(attribute.getUnitId());
				((RangeAttributeUpdatedEvent<Number>) event).setFrom(from);
				((RangeAttributeUpdatedEvent<Number>) event).setTo(to);
			} else if (node instanceof TaxonAttribute) {
				TaxonAttribute taxonAttr = (TaxonAttribute) node;
				TaxonAttributeUpdatedEvent taxonEvent = new TaxonAttributeUpdatedEvent();
				taxonEvent.setCode(taxonAttr.getCode());
				taxonEvent.setScientificName(taxonAttr.getScientificName());
				taxonEvent.setVernacularName(taxonAttr.getVernacularName());
				taxonEvent.setLanguageCode(taxonAttr.getLanguageCode());
				taxonEvent.setLanguageVariety(taxonAttr.getLanguageVariety());
				event = taxonEvent;
			} else if (node instanceof TextAttribute) {
				event = new TextAttributeUpdatedEvent();
				((TextAttributeUpdatedEvent) event).setText(((TextAttribute) node).getText());
			} else if (node instanceof TimeAttribute) {
				event = new TimeAttributeUpdatedEvent();
				Time value = ((TimeAttribute) node).getValue();
				((TimeAttributeUpdatedEvent) event).setTime(value.toJavaDate());
//			} else {
//				TODO fail for not supported node types
//				throw new IllegalArgumentException("Unexpected node type: " + node.getClass().getSimpleName());
			}
			if (event != null) {
				fillRecordEvent(event);
				event.setValidationResults(
						new ValidationResultsView((Attribute<?, ?>) node, context.messageSource, context.locale));
				consumer.onEvent(event);
			}
		}

		void entityDeleted() {
			EntityDeletedEvent event = new EntityDeletedEvent();
			fillRecordEvent(event);
			consumer.onEvent(event);
		}

		void attributeDeleted() {
			AttributeDeletedEvent event = new AttributeDeletedEvent();
			fillRecordEvent(event);
			consumer.onEvent(event);
		}

		private void relevanceUpdated(int childDefinitionId, boolean relevant) {
			NodeRelevanceUpdatedEvent event = new NodeRelevanceUpdatedEvent();
			event.setChildDefinitionId(childDefinitionId);
			event.setRelevant(relevant);
			consumer.onEvent(fillRecordEvent(event));
		}

		private void minCountUpdated(int childDefinitionId, int minCount) {
			NodeMinCountUpdatedEvent event = new NodeMinCountUpdatedEvent();
			event.setChildDefinitionId(childDefinitionId);
			event.setCount(minCount);
			consumer.onEvent(fillRecordEvent(event));
		}

		private void maxCountUpdated(int childDefinitionId, int maxCount) {
			NodeMaxCountUpdatedEvent event = new NodeMaxCountUpdatedEvent();
			event.setChildDefinitionId(childDefinitionId);
			event.setCount(maxCount);
			consumer.onEvent(fillRecordEvent(event));
		}

		private void minCountValidationUpdated(int childDefinitionId, ValidationResultFlag flag) {
			NodeMinCountValidationUpdatedEvent event = new NodeMinCountValidationUpdatedEvent();
			event.setChildDefinitionId(childDefinitionId);
			event.setFlag(flag);
			consumer.onEvent(fillRecordEvent(event));
		}

		private void maxCountValidationUpdated(int childDefinitionId, ValidationResultFlag flag) {
			NodeMaxCountValidationUpdatedEvent event = new NodeMaxCountValidationUpdatedEvent();
			event.setChildDefinitionId(childDefinitionId);
			event.setFlag(flag);
			consumer.onEvent(fillRecordEvent(event));
		}

		private <E extends RecordEvent> E fillRecordEvent(E event) {
			event.setSurveyName(node.getSurvey().getName());
			event.setRecordId(recordId);
			event.setRecordStep(recordStep);
			event.setDefinitionId(String.valueOf(node.getDefinition().getId()));
			event.setAncestorIds(ancestorIds);
			event.setNodeId(String.valueOf(node.getInternalId()));
			event.setNodePath(node.getPath());
			event.setParentEntityPath(node.getParent() == null ? null : node.getParent().getPath());
			event.setTimestamp(timestamp);
			event.setUserName(context.userName);
			return event;
		}
	}

	public static class EventProducerContext {
		MessageSource messageSource;
		Locale locale;
		String userName;

		public EventProducerContext(MessageSource messageSource, Locale locale, String userName) {
			super();
			this.messageSource = messageSource;
			this.locale = locale;
			this.userName = userName;
		}
	}

}
