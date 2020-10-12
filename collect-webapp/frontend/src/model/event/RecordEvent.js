import { Event } from './Event'
import Serializable from '../Serializable'

export class RecordEvent extends Event {
  eventType
  surveyName
  recordId
  recordStep
  definitionId
  ancestorIds
  nodeId
  nodePath
  timestamp
  userName

  static TYPE = 'recordEvent'

  constructor(jsonObj) {
    super()
    if (jsonObj) {
      this.fillFromJSON(jsonObj)
    }
  }

  /**
   * Returns the first not NaN id in ancestorIds
   * (NaN ids are for entity containers)
   */
  get parentEntityId() {
    const result = this.ancestorIds.find((id) => !isNaN(id))
    return result ? parseInt(result, 10) : NaN
  }

  isRelativeToRecord(record) {
    return this.recordId === record.id && this.recordStep === record.step
  }

  isRelativeToNode(node) {
    return this.isRelativeToRecord(node.record) && this.nodePath === node.path
  }

  isRelativeToNodes({ parentEntity, nodeDefId }) {
    return (
      this.isRelativeToRecord(parentEntity.record) &&
      this.parentEntityPath === parentEntity.path &&
      Number(this.definitionId) === nodeDefId
    )
  }
}

export class AttributeEvent extends RecordEvent {}

export class AttributeDeletedEvent extends RecordEvent {}

export class AttributeValueUpdatedEvent extends AttributeEvent {
  value
}

export class BooleanAttributeUpdatedEvent extends AttributeValueUpdatedEvent {}

export class CodeAttributeUpdatedEvent extends AttributeValueUpdatedEvent {}

export class CoordinateAttributeUpdatedEvent extends AttributeValueUpdatedEvent {}

export class DateAttributeUpdatedEvent extends AttributeValueUpdatedEvent {}

export class DoubleAttributeUpdatedEvent extends AttributeValueUpdatedEvent {}

export class DoubleRangeAttributeUpdatedEvent extends AttributeValueUpdatedEvent {}

export class IntegerAttributeUpdatedEvent extends AttributeValueUpdatedEvent {}

export class IntegerRangeAttributeUpdatedEvent extends AttributeValueUpdatedEvent {}

export class TaxonAttributeUpdatedEvent extends AttributeValueUpdatedEvent {}

export class TextAttributeUpdatedEvent extends AttributeValueUpdatedEvent {}

export class TimeAttributeUpdatedEvent extends AttributeValueUpdatedEvent {}

export class NodeRelevanceUpdatedEvent extends RecordEvent {
  childDefinitionId
  relevant
}

export class NodeCountUpdatedEvent extends RecordEvent {
  childDefinitionId
  count
}

export class NodeMaxCountUpdatedEvent extends NodeCountUpdatedEvent {}

export class NodeMinCountUpdatedEvent extends NodeCountUpdatedEvent {}

export class NodeCountValidationUpdatedEvent extends RecordEvent {
  childDefinitionId
  flag
}

export class NodeMaxCountValidationUpdatedEvent extends NodeCountValidationUpdatedEvent {}

export class NodeMinCountValidationUpdatedEvent extends NodeCountValidationUpdatedEvent {}

export class EntityCreatedEvent extends RecordEvent {}

export class EntityCollectionCreatedEvent extends RecordEvent {}

export class EntityDeletedEvent extends RecordEvent {}

const EVENT_CLASS_BY_TYPE = {
  EntityCreatedEvent,
  EntityCollectionCreatedEvent,
  EntityDeletedEvent,
  NodeMaxCountUpdatedEvent,
  NodeMaxCountValidationUpdatedEvent,
  NodeMinCountUpdatedEvent,
  NodeMinCountValidationUpdatedEvent,
  NodeRelevanceUpdatedEvent,
  AttributeDeletedEvent,
  BooleanAttributeUpdatedEvent,
  CodeAttributeUpdatedEvent,
  CoordinateAttributeUpdatedEvent,
  DateAttributeUpdatedEvent,
  DoubleAttributeUpdatedEvent,
  DoubleRangeAttributeUpdatedEvent,
  IntegerAttributeUpdatedEvent,
  IntegerRangeAttributeUpdatedEvent,
  TaxonAttributeUpdatedEvent,
  TextAttributeUpdatedEvent,
  TimeAttributeUpdatedEvent,
}

export class RecordEventWrapper extends Serializable {
  event
  eventType

  constructor(jsonObj) {
    super()
    if (jsonObj) {
      this.fillFromJSON(jsonObj)
    }
  }

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj)
    this.event = this._parseEvent(jsonObj)
  }

  _parseEvent(jsonObj) {
    const { eventType, event } = jsonObj
    const eventClass = EVENT_CLASS_BY_TYPE[eventType]
    if (eventClass) {
      return new eventClass(event)
    } else {
      console.log('Unsupported event type: ' + eventType)
    }
  }
}
