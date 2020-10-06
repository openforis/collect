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
  timestamp
  userName

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
    let result = this.ancestorIds.find((id) => !isNaN(id))
    return result ? parseInt(result, 10) : NaN
  }
}

export class AttributeEvent extends RecordEvent {}

export class AttributeDeletedEvent extends RecordEvent {}

export class AttributeUpdatedEvent extends AttributeEvent {}

export class BooleanAttributeUpdatedEvent extends AttributeUpdatedEvent {
  value
}

export class CodeAttributeUpdatedEvent extends AttributeUpdatedEvent {
  code
  qualifier
}

export class CoordinateAttributeUpdatedEvent extends AttributeUpdatedEvent {
  x
  y
  srsId
}

export class DateAttributeUpdatedEvent extends AttributeUpdatedEvent {
  date
}

export class DoubleAttributeUpdatedEvent extends AttributeUpdatedEvent {
  value
}

export class DoubleRangeAttributeUpdatedEvent extends AttributeUpdatedEvent {
  from
  to
}

export class IntegerAttributeUpdatedEvent extends AttributeUpdatedEvent {
  value
}

export class IntegerRangeAttributeUpdatedEvent extends AttributeUpdatedEvent {
  from
  to
}

export class TaxonAttributeUpdatedEvent extends AttributeUpdatedEvent {
  code
  scientificName
  vernacularName
  languageCode
  languageVariety
}

export class TextAttributeUpdatedEvent extends AttributeUpdatedEvent {
  text
}

export class TimeAttributeUpdatedEvent extends AttributeUpdatedEvent {
  time
}

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
