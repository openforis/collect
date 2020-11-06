import { Entity } from './Record'

import {
  AttributeDeletedEvent,
  AttributeValueUpdatedEvent,
  EntityCreatedEvent,
  EntityDeletedEvent,
  NodeRelevanceUpdatedEvent,
  NodeMinCountUpdatedEvent,
  NodeMinCountValidationUpdatedEvent,
  NodeMaxCountUpdatedEvent,
  NodeMaxCountValidationUpdatedEvent,
  RecordEvent,
  AttributeCreatedEvent,
} from './event/RecordEvent'

import EventQueue from './event/EventQueue'

export class RecordUpdater {
  record = null

  constructor(record) {
    this.record = record

    this.onRecordEvent = this.onRecordEvent.bind(this)

    EventQueue.subscribe(RecordEvent.TYPE, this.onRecordEvent)
  }

  destroy() {
    EventQueue.unsubscribe(RecordEvent.TYPE, this.onRecordEvent)
  }

  onRecordEvent(event) {
    const record = this.record
    if (record && event.isRelativeToRecord(record)) {
      const survey = record.survey
      const definition = survey.schema.getDefinitionById(Number(event.definitionId))
      const parentEntity = event.parentEntityPath ? record.getNodeByPath(event.parentEntityPath) : record.rootEntity
      const node = record.getNodeByPath(event.nodePath)

      if (event instanceof EntityCreatedEvent) {
        const newEntity = new Entity(record, definition, parentEntity)
        newEntity.childrenRelevanceByDefinitionId = event.childrenRelevanceByDefinitionId
        newEntity.childrenMinCountByDefinitionId = event.childrenMinCountByDefinitionId
        newEntity.childrenMaxCountByDefinitionId = event.childrenMaxCountByDefinitionId
        newEntity.childrenMinCountValidationByDefinitionId = event.childrenMinCountValidationByDefinitionId
        newEntity.childrenMaxCountValidationByDefinitionId = event.childrenMaxCountValidationByDefinitionId
        parentEntity.addChild(newEntity)
      } else if (event instanceof AttributeCreatedEvent || event instanceof AttributeValueUpdatedEvent) {
        let attr = node
        if (attr == null) {
          attr = parentEntity.addNewAttribute(definition)
        }
        attr.value = event.value
        attr.validationResults = event.validationResults
      } else if (event instanceof AttributeDeletedEvent || event instanceof EntityDeletedEvent) {
        parentEntity.removeChild(node)
      } else if (event instanceof NodeRelevanceUpdatedEvent) {
        node.childrenRelevanceByDefinitionId[event.childDefinitionId] = event.relevant
      } else if (event instanceof NodeMinCountUpdatedEvent) {
        node.childrenMinCountByDefinitionId[event.childDefinitionId] = event.count
      } else if (event instanceof NodeMaxCountUpdatedEvent) {
        node.childrenMaxCountByDefinitionId[event.childDefinitionId] = event.count
      } else if (event instanceof NodeMinCountValidationUpdatedEvent) {
        node.childrenMinCountValidationByDefinitionId[event.childDefinitionId] = event.flag
      } else if (event instanceof NodeMaxCountValidationUpdatedEvent) {
        node.childrenMaxCountValidationByDefinitionId[event.childDefinitionId] = event.flag
      }
    }
  }
}
