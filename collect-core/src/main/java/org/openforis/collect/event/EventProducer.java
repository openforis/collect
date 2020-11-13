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
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.DateAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.FileAttribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodeVisitor;
import org.openforis.idm.model.NumberAttribute;
import org.openforis.idm.model.NumericRangeAttribute;
import org.openforis.idm.model.TaxonAttribute;
import org.openforis.idm.model.TextAttribute;
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
						factory.attributeValueUpdated();
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

		String parentEntityPath = change instanceof NodeDeleteChange ? ((NodeDeleteChange) change).getParentEntityPath()
				: node.getParent() == null ? null : node.getParent().getPath();
		EventFactory factory = new EventFactory(recordId, recordStep, ancestorIds, parentEntityPath, node);

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
				factory.attributeValueUpdated();
			}
		} else if (change instanceof NodeDeleteChange) {
			factory.nodeDeleted();
		}

	}

	private List<String> getAncestorIds(Node<?> node) {
		return getAncestorIds(node.getDefinition(), node.getAncestorIds());
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
		String parentEntityPath;
		Node<?> node;
		Date timestamp;

		EventFactory(Integer recordId, RecordStep recordStep, List<String> ancestorIds, Node<?> node) {
			this(recordId, recordStep, ancestorIds, node.getParent().getPath(), node);
		}

		EventFactory(Integer recordId, RecordStep recordStep, List<String> ancestorIds, String parentEntityPath,
				Node<?> node) {
			this.recordId = recordId;
			this.recordStep = recordStep;
			this.ancestorIds = ancestorIds;
			this.parentEntityPath = parentEntityPath;
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

			entity.traverseDescendants(new NodeVisitor() {
				public void visit(Node<? extends NodeDefinition> descendant, int idx) {
					EventFactory descendantEventFactory = new EventFactory(recordId, recordStep,
							getAncestorIds(descendant), descendant);
					RecordEvent event;
					if (descendant instanceof Entity) {
						Entity entity = (Entity) descendant;
						EntityCreatedEvent entityEvent = new EntityCreatedEvent();
						entityEvent.setChildrenRelevanceByDefinitionId(entity.getRelevanceByDefinitionId());
						entityEvent.setChildrenMinCountByDefinitionId(entity.getMinCountByDefinitionId());
						entityEvent.setChildrenMaxCountByDefinitionId(entity.getMaxCountByDefinitionId());
						entityEvent.setChildrenMinCountValidationByDefinitionId(
								entity.getMinCountValidationResultByDefinitionId());
						entityEvent.setChildrenMaxCountValidationByDefinitionId(
								entity.getMaxCountValidationResultByDefinitionId());
						event = entityEvent;
						descendantEventFactory.fillRecordEvent(event);
						consumer.onEvent(event);
					} else {
						descendantEventFactory.attributeCreated();
					}
				}
			});

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
			EntityCreationCompletedEvent creationCompleteEvent = new EntityCreationCompletedEvent();
			fillRecordEvent(creationCompleteEvent);
			consumer.onEvent(creationCompleteEvent);
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
			AttributeCreatedEvent event = new AttributeCreatedEvent();
			fillRecordEvent(event);
			Attribute<?, ?> attribute = (Attribute<?, ?>) node;
			if (node.hasData()) {
				event.setValue(attribute.getValue());
			}
			event.setValidationResults(new ValidationResultsView(attribute, context.messageSource, context.locale));
			consumer.onEvent(event);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		void attributeValueUpdated() {
			AttributeValueUpdatedEvent event = null;
			Attribute attribute = (Attribute) node;
			if (attribute instanceof BooleanAttribute) {
				event = new BooleanAttributeUpdatedEvent();
			} else if (attribute instanceof CodeAttribute) {
				event = new CodeAttributeUpdatedEvent();
			} else if (attribute instanceof CoordinateAttribute) {
				event = new CoordinateAttributeUpdatedEvent();
			} else if (attribute instanceof DateAttribute) {
				event = new DateAttributeUpdatedEvent();
			} else if (attribute instanceof FileAttribute) {
				event = new FileAttributeUpdatedEvent();
			} else if (attribute instanceof NumberAttribute<?, ?>) {
				Type valueType = ((NumericAttributeDefinition) attribute.getDefinition()).getType();
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
			} else if (attribute instanceof NumericRangeAttribute<?, ?>) {
				Type valueType = ((RangeAttributeDefinition) attribute.getDefinition()).getType();
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
			} else if (attribute instanceof TaxonAttribute) {
				event = new TaxonAttributeUpdatedEvent();
			} else if (attribute instanceof TextAttribute) {
				event = new TextAttributeUpdatedEvent();
			} else if (attribute instanceof TimeAttribute) {
				event = new TimeAttributeUpdatedEvent();
			}
			event.setValue(attribute.getValue());
			fillRecordEvent(event);
			event.setValidationResults(new ValidationResultsView(attribute, context.messageSource, context.locale));
			consumer.onEvent(event);
		}

		void nodeDeleted() {
			RecordEvent event = node instanceof Entity ? new EntityDeletedEvent() : new AttributeDeletedEvent();
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
			event.setParentEntityPath(parentEntityPath);
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
