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

const eventClassByType = (type) => ({
  EntityCreatedEvent: EntityCreatedEvent,
  EntityCollectionCreatedEvent: EntityCollectionCreatedEvent,
  NodeMaxCountUpdatedEvent: NodeMaxCountUpdatedEvent,
  NodeMaxCountValidationUpdatedEvent: NodeMaxCountValidationUpdatedEvent,
  NodeMinCountUpdatedEvent: NodeMinCountUpdatedEvent,
  NodeMinCountValidationUpdatedEvent: NodeMinCountValidationUpdatedEvent,
  NodeRelevanceUpdatedEvent: NodeRelevanceUpdatedEvent,
  BooleanAttributeUpdatedEvent: BooleanAttributeUpdatedEvent,
  CodeAttributeUpdatedEvent: CodeAttributeUpdatedEvent,
  CoordinateAttributeUpdatedEvent: CoordinateAttributeUpdatedEvent,
  DateAttributeUpdatedEvent: DateAttributeUpdatedEvent,
  DoubleAttributeUpdatedEvent: DoubleAttributeUpdatedEvent,
  DoubleRangeAttributeUpdatedEvent: DoubleRangeAttributeUpdatedEvent,
  IntegerAttributeUpdatedEvent: IntegerAttributeUpdatedEvent,
  IntegerRangeAttributeUpdatedEvent: IntegerRangeAttributeUpdatedEvent,
  TaxonAttributeUpdatedEvent: TaxonAttributeUpdatedEvent,
  TextAttributeUpdatedEvent: TextAttributeUpdatedEvent,
  TimeAttributeUpdatedEvent: TimeAttributeUpdatedEvent,
})

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
    switch (eventType) {
      case 'EntityCreatedEvent':
        return new EntityCreatedEvent(event)
      case 'EntityCollectionCreatedEvent':
        return new EntityCreatedEvent(event)
      case 'NodeMaxCountUpdatedEvent':
        return new NodeMaxCountUpdatedEvent(event)
      case 'NodeMaxCountValidationUpdatedEvent':
        return new NodeMaxCountValidationUpdatedEvent(event)
      case 'NodeMinCountUpdatedEvent':
        return new NodeMinCountUpdatedEvent(jsonObj.event)
      case 'NodeMinCountValidationUpdatedEvent':
        return new NodeMinCountValidationUpdatedEvent(jsonObj.event)
      case 'NodeRelevanceUpdatedEvent':
        return new NodeRelevanceUpdatedEvent(jsonObj.event)
      case 'BooleanAttributeUpdatedEvent':
        return new BooleanAttributeUpdatedEvent(jsonObj.event)
      case 'CodeAttributeUpdatedEvent':
        return new CodeAttributeUpdatedEvent(jsonObj.event)
      case 'CoordinateAttributeUpdatedEvent':
        return new CoordinateAttributeUpdatedEvent(jsonObj.event)
      case 'DateAttributeUpdatedEvent':
        return new DateAttributeUpdatedEvent(jsonObj.event)
      case 'DoubleAttributeUpdatedEvent':
        return new DoubleAttributeUpdatedEvent(jsonObj.event)
      case 'DoubleRangeAttributeUpdatedEvent':
        return new DoubleRangeAttributeUpdatedEvent(jsonObj.event)
      case 'IntegerAttributeUpdatedEvent':
        return new IntegerAttributeUpdatedEvent(jsonObj.event)
      case 'IntegerRangeAttributeUpdatedEvent':
        return new IntegerRangeAttributeUpdatedEvent(jsonObj.event)
      case 'TaxonAttributeUpdatedEvent':
        return new TaxonAttributeUpdatedEvent(jsonObj.event)
      case 'TextAttributeUpdatedEvent':
        return new TextAttributeUpdatedEvent(jsonObj.event)
      case 'TimeAttributeUpdatedEvent':
        return new TimeAttributeUpdatedEvent(jsonObj.event)
      default:
        console.log('Unsupported event type: ' + jsonObj.eventType)
        return null //TODO throw error?
    }
  }
}
